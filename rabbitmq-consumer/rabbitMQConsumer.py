import pika
import json
import time


class RabbitMQConsumer:
    @staticmethod
    def process(message: dict, functions: dict):
        functions.update(globals())
        if 'function' not in message.keys():
            return
        functions[message['function']](*message['args'])

    def consume_queue(self, queue: str, functions: dict):
        while True:
            try:
                conn = pika.BlockingConnection(pika.ConnectionParameters('rabbitmq'))
            except pika.exceptions.AMQPConnectionError:
                print(f"Retrying connection for queue {queue}...")
                time.sleep(5)
            else:
                break
        channel = conn.channel()
        channel.queue_declare(queue=queue, durable=True)
        for method, properties, body in channel.consume(queue=queue, inactivity_timeout=30):
            if body:
                res = json.loads(body.decode())
                self.process(res, functions)

                # We signal that the message is received and processed, rabbitMQ will now remove it from the queue
                channel.basic_ack(delivery_tag=method.delivery_tag)
