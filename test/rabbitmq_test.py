import unittest
import pika


class MyTestCase(unittest.TestCase):

    def test_produce_consume(self):
        conn = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
        channel = conn.channel()
        channel.queue_declare(queue='hello')
        channel.basic_publish(exchange='',
                              routing_key='hello',
                              body='Hello World!')

        result = channel.consume(queue='hello')
        message = next(result)
        conn.close()
        self.assertEqual(message[2].decode(), "Hello World!")


if __name__ == '__main__':
    unittest.main()
