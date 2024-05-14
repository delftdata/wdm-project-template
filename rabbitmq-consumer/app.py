import logging
import time
import threading
from rabbitMQConsumer import RabbitMQConsumer


# Example function. You can put RabbitMQ, POST and GET requests to communicate with apps.
def hello_world(hello, world):
    print(f"{hello}, {world}")


consumer = RabbitMQConsumer()

if __name__ == '__main__':
    queues = ['payment', 'order', 'stock', 'test']
    threads = {}
    for q in queues:
        threads[q] = threading.Thread(target=consumer.consume_queue, args=(q, globals()), daemon=True)
        threads[q].start()

    while True:
        # Restart if heartbeat stopped
        for q, t in threads.items():
            if not t.is_alive():
                t.start()

        time.sleep(60)

