import asyncio
import contextlib
import json
from typing import (Any, Callable, Coroutine, List, MutableMapping, ParamSpec,
                    TypeVar)
from uuid import uuid4

from aio_pika import Message, connect_robust
from aio_pika.abc import AbstractIncomingMessage

from order.db import Session
from order.model import AMQPMessage
from order.services import (create_order,update_order, order_details_by_order_ref_no, Order)

P = ParamSpec('P')
T = TypeVar('T')


class SagaReplyHandler:

    reply_status: str | None
    action: Coroutine
    is_compensation: bool = False

    def __init__(
        self, reply_status: str, action: Coroutine,
        is_compensation: bool = False
    ) -> None:
        self.reply_status = reply_status
        self.action = action
        self.is_compensation = is_compensation


class SagaRPC:

    data: Any = None

    def __init__(self) -> None:
        self.futures: MutableMapping[str, asyncio.Future] = {}
        self.loop = asyncio.get_running_loop()

    @contextlib.asynccontextmanager
    async def connect(self) -> "SagaRPC":
        try:
            self.connection = await connect_robust(
                settings.RABBITMQ_BROKER_URL, loop=self.loop,
            )
            self.channel = await self.connection.channel()

            self.exchange = await self.channel.declare_exchange(
                'ORDER_TX_EVENT_STORE',
                type='topic',
                durable=True
            )

            self.callback_queue = await self.channel.declare_queue(exclusive=True)
            await self.callback_queue.bind(self.exchange)
            await self.callback_queue.consume(self.reply_event_processor)

            yield self

        finally:
            await self.connection.close()

    def reply_event_processor(self, message: AbstractIncomingMessage) -> None:
        if message.correlation_id is None:
            print(f'Bad message {message!r}')
            return

        try:
            future: asyncio.Future = self.futures.pop(message.correlation_id)
            future.set_result(message.body)
        except KeyError:
            print(f'Unknown correlation_id! - {message.correlation_id}')

    async def start_workflow(self) -> Any:
        for step_definition in await self.definitions:
            is_step_success = await step_definition
            if not is_step_success:
                break

        # If request booking workflow succeeded we can return the data
        return self.data

    async def invoke_local(self, action: Callable[P, T]):
        return await action()

    async def invoke_participant(
        self, command: str, on_reply: List[SagaReplyHandler] | None = None
    ) -> bool:

        if on_reply is None:
            on_reply = []

        correlation_id = str(uuid4())
        future = self.loop.create_future()

        self.futures[correlation_id] = future

        await self.exchange.publish(
            Message(
                str(json.dumps(self.data.to_dict())).encode(),
                content_type='application/json',
                correlation_id=correlation_id,
                reply_to=self.callback_queue.name,
                headers={
                    'COMMAND': command.replace('.', '_').upper(),
                    'CLIENT': 'ORDER_REQUEST_ORCHESTRATOR',
                }
            ),
            routing_key=command,
        )

        # Wait for the reply event processor to received a reponse from
        # the participating service.
        response_data: bytes = await future
        decoded_data = json.loads(response_data.decode('utf-8'))
        reply_state = decoded_data.get('reply_state')

        # If response reply status execute a compensation command
        # we need to stop the succeeding step by returning `False`.
        to_next_definition = True
        for reply_handler in on_reply:
            saga_reply_handler: SagaReplyHandler = reply_handler

            if reply_state == saga_reply_handler.reply_status:
                if saga_reply_handler.is_compensation:
                    to_next_definition = False

                await saga_reply_handler.action

        return to_next_definition

    @property
    async def definitions(self) -> List[Coroutine]:
        raise NotImplementedError


class CreateOrderRequestSaga(SagaRPC):

    data: Order = None
    order_uuid: str = None

    def __init__(self, order_uuid: str) -> None:
        super().__init__()
        self.order_uuid = order_uuid

    @property
    async def definitions(self):
        return [
            self.invoke_local(action=self.create_order),
            self.invoke_participant(
                command='stock.block',
                on_reply=[
                    SagaReplyHandler(
                        'STOCK_UNAVAILABLE',
                        action=self.invoke_participant(
                            command='stock.unblock'
                        ),
                        is_compensation=True
                    ),
                ]
            ),
            self.invoke_participant(command='payment.authorize_payment'),
            self.invoke_participant(
                command='stock.subtract',
                on_reply=[
                    SagaReplyHandler(
                        'STOCK_SUBTRACT_FAILED',
                        action=self.invoke_participant(command='stock.unblock'),
                        is_compensation=True
                    ),
                    SagaReplyHandler(
                        'STOCK_SUBTRACT_FAILED',
                        action=self.invoke_participant(
                            command='stock.add',
                            on_reply=[
                                SagaReplyHandler(
                                    'STOCK_ADDED',
                                    action=self.invoke_local(action=self.failed_order),
                                    is_compensation=True
                                )
                            ]
                        ),
                        is_compensation=True
                    ),
                ]
            ),
            self.invoke_local(action=self.completed_order)
        ]

    async def create_order(self) -> bool:
        with Session() as session:
            self.data = await create_order(session, self.order_uuid)
            return self.data.id is not None

    async def completed_order(self) -> bool:
        with Session() as session:
            order = await order_details_by_order_ref_no(session, self.data.order_ref_no)
            order.status = 'completed'

            # Updated data
            self.data = await update_order(session, order)
            return self.data.status == 'completed'

    async def failed_order(self) -> bool:
        with Session() as session:
            order = await order_details_by_order_ref_no(session, self.data.order_ref_no)
            order.status = 'failed'

            # Updated data
            self.data = await update_order(session, order)
            return self.data.status == 'failed'