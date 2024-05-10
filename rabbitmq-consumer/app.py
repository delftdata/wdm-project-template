import logging
import time

import pika
import threading
import json

# Example function. You can put POST and GET requests in your functions to properly communicate with flask apps.
def hello_world(hello, world):
    print(f"{hello}, {world}")


def process(message: dict):
    if 'function' not in message.keys():
        return
    globals()[message['function']](*message['args'])


def consume_queue(queue: str):
    conn = pika.BlockingConnection(pika.ConnectionParameters('rabbitmq'))
    channel = conn.channel()
    channel.queue_declare(queue=queue)
    for method, properties, body in channel.consume(queue=queue):
        res = json.loads(body.decode())
        process(res)


if __name__ == '__main__':
    queues = ['payment', 'order', 'stock', 'test']
    threads = {}
    for q in queues:
        threads[q] = threading.Thread(target=consume_queue, args=(q,), daemon=True)
        threads[q].start()

    while True:
        print(threads)
        time.sleep(60)
