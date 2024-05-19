import atexit
import os
from typing import List
from uuid import uuid4

import redis
from aio_pika import IncomingMessage
from msgspec import msgpack, Struct

from order.model import AMQPMessage
from order.amqp_client import AMQPClient
from order.exceptions import RedisDBError, InsufficientCreditError

db: redis.Redis = redis.Redis(host=os.environ['REDIS_HOST'],
                              port=int(os.environ['REDIS_PORT']),
                              password=os.environ['REDIS_PASSWORD'],
                              db=int(os.environ['REDIS_DB']))

def close_db_connection():
    db.close()

atexit.register(close_db_connection)

async def order_details(session: Session, id: str) -> Booking:
    return session.query(Order).filter(Order.id == id).one()


class Order:
    pass


async def order_details_by_order_ref_no(session: Session, parking_slot_ref_no: str) -> Order:
    return session.query(Order).filter(Order.order_ref_no == order_ref_no).one()


async def order_list(session: Session) -> List[Order]:
    return session.query(Order).all()


async def create_order(order_uuid: str) -> Order:
    # Since customers may happen to book the same parking slot,
    # we need to include unique booking identifier (uuid4) to parking_slot_ref_no.
    # The booking identifier will be used throughout the services to identify
    # transaction.
    order = Order(
        order_ref_no=f'{order_uuid}:{uuid4()}',
        status='pending'
    )
    session.add(order)
    session.commit()
    session.refresh(order)

    return order


async def update_order(session: Session, order: Order) -> Order:
    session.commit()
    session.refresh(order)
    return order