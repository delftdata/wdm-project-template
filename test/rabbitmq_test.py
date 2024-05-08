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


if __name__ == '__main__':
    unittest.main()
