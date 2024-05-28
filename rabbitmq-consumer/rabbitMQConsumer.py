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

    def send_status(self, queue: str, message: dict):
        # Establish a connection and channel for sending status messages
        while True:
            try:
                status_conn = pika.BlockingConnection(pika.ConnectionParameters('rabbitmq'))
            except pika.exceptions.AMQPConnectionError:
                print(f"Retrying connection for queue {queue}...")
                time.sleep(5)
            else:
                break
        status_channel = status_conn.channel()
        status_channel.queue_declare(queue=queue, durable=True)

        # Publish the status message
        status_channel.basic_publish(
            exchange='',
            routing_key=queue,
            body=json.dumps(message),
            properties=pika.BasicProperties(
                delivery_mode=2,  # Persistent message
                correlation_id=message['correlation_id']
            )
        )


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

                # Send the status message to the status queue
                status_message = {
                    'status': 'Processed',
                    'correlation_id': properties.correlation_id
                }
                self.send_status(properties.reply_to, status_message)

                # We signal that the message is received and processed, rabbitMQ will now remove it from the queue
                channel.basic_ack(delivery_tag=method.delivery_tag)
