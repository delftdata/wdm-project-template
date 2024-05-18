import logging
import time
import threading
import requests
from collections import defaultdict
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


def handle_checkout(order_id: str, user_id: str, items: list, total_cost: float):
    print(f"Handling checkout for {order_id}")

    # Calculate the quantity per item
    items_quantities = defaultdict(int)
    for item_id, quantity in items:
        items_quantities[item_id] += quantity

    removed_items = []

    try:
        # Subtract stock for each item
        for item_id, quantity in items_quantities.items():
            stock_reply = requests.post(f"{GATEWAY_URL}/stock/subtract/{item_id}/{quantity}")
            if stock_reply.status_code != 200:
                rollback_stock(removed_items)
                print(f"Out of stock on item_id: {item_id}")
                return

            removed_items.append((item_id, quantity))

        # Process payment
        payment_reply = requests.post(f"{GATEWAY_URL}/payment/pay/{user_id}/{total_cost}")
        if payment_reply.status_code != 200:
            rollback_stock(removed_items)
            print("User out of credit")
            return

        # Update order status to paid
        order_update_reply = requests.post(f"{GATEWAY_URL}/orders/checkoutProcess/{order_id}")
        if order_update_reply.status_code != 200:
            rollback_stock(removed_items)
            print("Failed to update order status")
            return

        print("Checkout handled successfully")
    
    except Exception as e:
        rollback_stock(removed_items)
        print(f"Failed to handle checkout: {str(e)}")

def rollback_stock(removed_items: list):
    for item_id, quantity in removed_items:
        requests.post(f"{GATEWAY_URL}/stock/add/{item_id}/{quantity}")

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

