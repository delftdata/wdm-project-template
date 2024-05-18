import logging
import uuid
import redis
import os
import atexit

from msgspec import msgpack, Struct
from flask import Flask, jsonify, abort, Response

from payment.services import (get_user_db, 
                      create_user_db, 
                      batch_init_db, 
                      add_credit_db, 
                      remove_credit_db)

from payment.exceptions import RedisDBError, InsufficientCreditError

app = Flask("payment-service")

class UserValue(Struct):
    credit: int

DB_ERROR_STR = "DB error"

async def get_user_from_db(user_id: str) -> UserValue | None:
    try:
        entry = await get_user_db(user_id)
    except RedisDBError:
        return abort(400, DB_ERROR_STR)
    if entry is None:
        abort(400, f"User: {user_id} not found!")
    return entry


@app.post('/create_user')
async def create_user():
    key = str(uuid.uuid4())
    value = msgpack.encode(UserValue(credit=0))
    try:
        await create_user_db(key, value)
    except RedisDBError:
        return abort(400, DB_ERROR_STR)
    return jsonify({'user_id': key})


@app.post('/batch_init/<n>/<starting_money>')
async def batch_init_users(n: int, starting_money: int):
    n = int(n)
    starting_money = int(starting_money)
    kv_pairs: dict[str, bytes] = {f"{i}": msgpack.encode(UserValue(credit=starting_money))
                                  for i in range(n)}
    try:
        await batch_init_db(kv_pairs)
    except RedisDBError:
        return abort(400, DB_ERROR_STR)
    return jsonify({"msg": "Batch init for users successful"})


@app.get('/find_user/<user_id>')
async def find_user(user_id: str):
    user_entry: UserValue = get_user_from_db(user_id)
    return jsonify(
        {
            "user_id": user_id,
            "credit": user_entry.credit
        }
    )


@app.post('/add_funds/<user_id>/<amount>')
async def add_credit(user_id: str, amount: int):
    try:
        user_entry = await add_credit_db(user_id, amount)
    except RedisDBError:
        return abort(400, DB_ERROR_STR)
    return Response(f"User: {user_id} credit updated to: {user_entry.credit}", status=200)


@app.post('/pay/<user_id>/<amount>')
async def remove_credit(user_id: str, amount: int):
    app.logger.debug(f"Removing {amount} credit from user: {user_id}")
    try:
        user_entry = await remove_credit_db(user_id, amount)
    except RedisDBError:
        return abort(400, DB_ERROR_STR)
    except InsufficientCreditError:
        abort(400, f"User: {user_id} credit cannot get reduced below zero!")
    return Response(f"User: {user_id} credit updated to: {user_entry.credit}", status=200)


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=8000, debug=True)
else:
    gunicorn_logger = logging.getLogger('gunicorn.error')
    app.logger.handlers = gunicorn_logger.handlers
    app.logger.setLevel(gunicorn_logger.level)
