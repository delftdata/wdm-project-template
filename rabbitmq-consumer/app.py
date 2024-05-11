import logging
import time

import pika
import threading
import json


# Example function. You can put RabbitMQ, POST and GET requests to communicate with apps.
def hello_world(hello, world):
    print(f"{hello}, {world}")


def process(message: dict):
    if 'function' not in message.keys():
        return
    globals()[message['function']](*message['args'])


def consume_queue(queue: str):
    while True:
        try:
            conn = pika.BlockingConnection(pika.ConnectionParameters('rabbitmq'))
        except pika.exceptions.AMQPConnectionError:
            print(f"Retrying connection for queue {queue}...")
            time.sleep(5)
        else:
            break
    channel = conn.channel()
    channel.queue_declare(queue=queue)
    for method, properties, body in channel.consume(queue=queue, inactivity_timeout=120):
        if body:
            res = json.loads(body.decode())
            process(res)


if __name__ == '__main__':
    queues = ['payment', 'order', 'stock', 'test']
    threads = {}
    for q in queues:
        threads[q] = threading.Thread(target=consume_queue, args=(q,), daemon=True)
        threads[q].start()

    while True:
        # Restart if heartbeat stopped
        for q, t in threads.items():
            if not t.is_alive():
                t.start()

        time.sleep(60)
