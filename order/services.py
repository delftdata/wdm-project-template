import atexit
import os
from typing import List
from uuid import uuid4
import uuid

import redis
from aio_pika import IncomingMessage
from msgspec import msgpack, Struct

from model import AMQPMessage
from amqp_client import AMQPClient
from exceptions import RedisDBError, InsufficientCreditError

db: redis.Redis = redis.Redis(host=os.environ['REDIS_HOST'],
                              port=int(os.environ['REDIS_PORT']),
                              password=os.environ['REDIS_PASSWORD'],
                              db=int(os.environ['REDIS_DB']))

def close_db_connection():
    db.close()

atexit.register(close_db_connection)

# async def order_details(session: Session, id: str) -> Booking:
#     return session.query(Order).filter(Order.id == id).one()


# class Order:
#     pass


# async def order_details_by_order_ref_no(session: Session, parking_slot_ref_no: str) -> Order:
#     return session.query(Order).filter(Order.order_ref_no == order_ref_no).one()


# async def order_list(session: Session) -> List[Order]:
#     return session.query(Order).all()


# async def create_order(order_uuid: str) -> Order:
#     # Since customers may happen to book the same parking slot,
#     # we need to include unique booking identifier (uuid4) to parking_slot_ref_no.
#     # The booking identifier will be used throughout the services to identify
#     # transaction.
#     order = Order(
#         order_ref_no=f'{order_uuid}:{uuid4()}',
#         status='pending'
#     )
#     session.add(order)
#     session.commit()
#     session.refresh(order)

#     return order


# async def update_order(session: Session, order: Order) -> Order:
#     session.commit()
#     session.refresh(order)
#     return order



class OrderValue(Struct):
    paid: bool
    items: list[tuple[str, int]]
    user_id: str
    total_cost: int

async def get_order_by_id_db(order_id: str) :
    try:
        entry: bytes = await db.get(order_id)
    except redis.exceptions.RedisError:
        raise RedisDBError(Exception)
    entry: OrderValue | None = msgpack.decode(entry, type=OrderValue) if entry else None
    return entry


async def create_order_db(user_id: str):
    key = str(uuid.uuid4())
    value = msgpack.encode(OrderValue(paid=False, items=[], user_id=user_id, total_cost=0))
    try:
        await db.set(key, value)
    except redis.exceptions.RedisError:
        raise RedisDBError(Exception)
    return key

async def batch_init_users_db(kv_pairs):
    try:
        await db.mset(kv_pairs)
    except redis.exceptions.RedisError:
        raise RedisDBError(Exception)
    
async def add_item_db(order_id, order_entry):
    try:
        await db.set(order_id, msgpack.encode(order_entry))
    except redis.exceptions.RedisError:
        raise RedisDBError(Exception)