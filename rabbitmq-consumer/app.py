import logging
import time
import threading
import requests
import os
from rabbitMQConsumer import RabbitMQConsumer

GATEWAY_URL = os.environ['GATEWAY_URL']


# Example function. You can put RabbitMQ, POST and GET requests to communicate with apps.
def hello_world(hello, world):
    print(f"{hello}, {world}")


def handle_add_item(order_id, item_id, quantity):
    response = requests.get(f"{GATEWAY_URL}/stock/find/{item_id.strip()}")
    if response.status_code == 200:
        item_details = response.json()
        price = int(item_details['price'])
        
        add_response = requests.post(f"{GATEWAY_URL}/orders/addItemProcess/{order_id.strip()}/{item_id.strip()}/{quantity.strip()}/{price}")
        if add_response.status_code == 200:
            print("Item added successfully to order")
        else:
            print(f"Failed to add item to order, status code: {add_response.status_code}, response: {add_response.text}")
    else:
        print("Failed to retrieve item details")


consumer = RabbitMQConsumer()

if __name__ == '__main__':
    queues = ['main', 'test']
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

