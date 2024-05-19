import logging
import os
import atexit
import logging
import os
import atexit
import uuid

import redis

from msgspec import msgpack, Struct
from exceptions import RedisDBError, InsufficientStockError
from flask import Flask, jsonify, abort, Response

class StockValue(Struct):
    stock: int
    price: int

db: redis.Redis = redis.Redis(host=os.environ['REDIS_HOST'],
                              port=int(os.environ['REDIS_PORT']),
                              password=os.environ['REDIS_PASSWORD'],
                              db=int(os.environ['REDIS_DB']))


def close_db_connection() -> StockValue | None:
    db.close()

atexit.register(close_db_connection)

async def get_item(item_id: str) -> str:
    try:
        return db.get(item_id)
    except redis.exceptions.RedisError:
        raise RedisDBError
    entry: StockValue | None = msgpack.decode(entry, type=StockValue) if entry else None
    if entry is None:
        # if item does not exist in the database; abort
        raise ItemNotFoundError
    return entry
        

async def set_new_item(price: int):
    key = str(uuid.uuid4())
    value = msgpack.encode(StockValue(stock=0, price=int(price)))
    try:
        db.set(key, value)
    except redis.exceptions.RedisError:
        raise RedisDBError
    return jsonify({'item_id': key})

async def set_users(n: int, starting_stock: int, item_price: int):
    n = int(n)
    starting_stock = int(starting_stock)
    item_price = int(item_price)
    kv_pairs: dict[str, bytes] = {f"{i}": msgpack.encode(StockValue(stock=starting_stock, price=item_price))
                                  for i in range(n)}
    try:
        db.mset(kv_pairs)
    except redis.exceptions.RedisError:
        raise RedisDBError
    return jsonify({"msg": "Batch init for stock successful"})


async def add_amount(item_id: str, amount: int):
    item_entry: StockValue = await get_item(item_id)
    # update stock, serialize and update database
    item_entry.stock += int(amount)
    try:
        db.set(item_id, msgpack.encode(item_entry))
    except redis.exceptions.RedisError:
        raise RedisDBError
    return Response(f"Item: {item_id} stock updated to: {item_entry.stock}", status=200)


async def remove_amount(item_id: str, amount: int):
    item_entry: StockValue = await get_item(item_id)
    item_entry.stock -= int(amount)
    # app.logger.debug(f"Item: {item_id} stock updated to: {item_entry.stock}")
    if item_entry.stock < 0:
        raise InsufficientStockError
        # abort(400, f"Item: {item_id} stock cannot get reduced below zero!")
    try:
        db.set(item_id, msgpack.encode(item_entry))
    except redis.exceptions.RedisError:
        raise RedisDBError
    return Response(f"Item: {item_id} stock updated to: {item_entry.stock}", status=200)



async def stock_command_event_processor(message: IncomingMessage):
    async with message.process(ignore_processed=True):
        command = message.headers.get('COMMAND')
        client = message.headers.get('CLIENT')

        stock_order = json.loads(str(message.body.decode('utf-8')))
        item_id = booking.get('item_id')
        amount = booking.get('amount')
        response_obj: AMQPMessage = None

        if client == 'ORDER_REQUEST_ORCHESTRATOR' and command == 'ADD_STOCK':
            reply_state="SUCCESSFUL"
            try:
                await add_amount(item_id, amount)  
            except RedisDBError:
                reply_state="UNSUCCESSFUL"
            except ItemNotFoundError:
                reply_state="UNSUCCESSFUL"
            await message.ack()
            response_obj = AMQPMessage(
                id=message.correlation_id,
                reply_state=reply_state
            )

        if client == 'ORDER_REQUEST_ORCHESTRATOR' and command == 'REMOVE_STOCK':
            reply_state = "SUCCESSFUL"
            try:
                is_unblock = await remove_amount(item_id, amount)
            except InsufficientStockError:
                reply_state = "UNSUCCESSFUL"
            await message.ack()
            response_obj = AMQPMessage(
                id=message.correlation_id,
                reply_state=reply_state
            )

        # There must be a response object to signal orchestrator of
        # the outcome of the request.
        assert response_obj is not None

        amqp_client: AMQPClient = await AMQPClient().init()
        await amqp_client.event_producer(
            'STOCK_EVENT_STORE',
            message.reply_to,
            message.correlation_id,
            response_obj
        )
        await amqp_client.connection.close()



