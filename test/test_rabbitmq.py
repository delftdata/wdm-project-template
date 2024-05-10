import unittest
import pika
import json


class RabbitMQExampleTest(unittest.TestCase):

    def test_produce_consume(self):
        conn = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
        channel = conn.channel()
        channel.queue_declare(queue='hello')

        # Do in source service
        channel.basic_publish(exchange='',
                              routing_key='hello',
                              body='Hello World!'.encode())

        # Do in destination service
        result = channel.consume(queue='hello')
        message = next(result)
        conn.close()
        self.assertEqual(message[2].decode(), "Hello World!")

    def test_json_example(self):
        conn = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
        message = {'id': 1, 'name': 'Hello World'}
        channel = conn.channel()
        channel.queue_declare(queue='json')

        # Do in source service
        channel.basic_publish(exchange='',
                              routing_key='json',
                              body=json.dumps(message).encode())

        # Do in destination service
        result = channel.consume(queue='json')
        res = json.loads(next(result)[2].decode())
        conn.close()
        self.assertEqual(res, message)

    def test_example_in_services(self):

        # Service 1, Publisher (order)
        conn = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
        message_1 = {'id': 1, 'type': 'deduct', 'Amount': 10}
        message_2 = {'id': 2, 'type': 'deduct', 'Amount': 25}
        channel = conn.channel()
        channel.queue_declare(queue='payment')
        channel.queue_purge(queue='payment')  # Ensure queue is purged for a clean slate (not for production)
        channel.basic_publish(exchange='',
                              routing_key='payment',
                              body=json.dumps(message_1).encode())
        channel.basic_publish(exchange='',
                              routing_key='payment',
                              body=json.dumps(message_2).encode())

        # Service 2, Consumer (payment)
        conn = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
        channel = conn.channel()
        channel.queue_declare(queue='payment')
        accounts = {1: {'Balance': 25}, 2: {'Balance': 50}}
        i = 0
        # (this loop is infinite and waits for messages)
        for method, properties, body in channel.consume(queue='payment'):
            res = json.loads(body.decode())

            # Stop early if we have received our two messages. Example action for processing
            accounts[res['id']]['Balance'] -= res['Amount']
            i += 1
            if i >= 2:
                conn.close()
                break

        # First account has properly been deducted
        self.assertEqual(accounts[1]['Balance'], 15)

        # The other account is also properly deducted
        self.assertEqual(accounts[2]['Balance'], 25)


if __name__ == '__main__':
    unittest.main()
