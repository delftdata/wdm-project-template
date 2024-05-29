import logging
import time
import threading
import requests
from collections import defaultdict
import os
from rabbitMQConsumer import RabbitMQConsumer

GATEWAY_URL = os.environ['GATEWAY_URL']
N_QUEUES = os.environ['MQ_REPLICAS']
REPLICA_INDEX = os.environ['REPLICA_INDEX']

# Example function. You can put RabbitMQ, POST and GET requests to communicate with apps.
def hello_world(hello, world):
    print(f"{hello}, {world}")

def get_queue_for_order(order_id):
    # print(int(N_QUEUES))
    print(order_id)
    print(hash(order_id))
    print(hash(order_id) % int(N_QUEUES))
    return hash(order_id) % int(N_QUEUES)

def handle_add_item(order_id, item_id, quantity):
    response = requests.get(f"{GATEWAY_URL}/stock/find/{item_id.strip()}")
    if response.status_code == 200:
        item_details = response.json()
        price = int(item_details['price'])

        add_response = requests.post(
            f"{GATEWAY_URL}/orders/addItemProcess/{order_id.strip()}/{item_id.strip()}/{quantity.strip()}/{price}")
        if add_response.status_code == 200:
            print(f"Item {item_id} added {quantity} times successfully to order")
        else:
            print(
                f"Failed to add item to order, status code: {add_response.status_code}, response: {add_response.text}")
    else:
        print("Failed to retrieve item details")


def handle_checkout(order_id: str):
    
    print(f"The hash for the order_id is: " + str(get_queue_for_order(order_id)))
    order_entry = requests.get(f"{GATEWAY_URL}/orders/find/{order_id}").json()
    user_id, items, total_cost = order_entry["user_id"], order_entry["items"], order_entry["total_cost"]
    print(f"Handling checkout for {order_id}, {items}")

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
            print(f"User out of credit: {user_id}")
            return

        # Update order status to paid
        order_update_reply = requests.post(f"{GATEWAY_URL}/orders/checkoutProcess/{order_id}")
        if order_update_reply.status_code != 200:
            rollback_stock(removed_items)
            print(f"Failed to update order status: {order_id}")
            return

        print(f"Checkout handled successfully: {order_id}")

    except Exception as e:
        rollback_stock(removed_items)
        print(f"Failed to handle checkout: {str(e)}")


def rollback_stock(removed_items: list):
    for item_id, quantity in removed_items:
        print(f"Rollback {item_id} {quantity} times")
        current_stock = requests.get(f"{GATEWAY_URL}/stock/find/{item_id}").json()["stock"]
        print(f"Stock of {item_id} before rollback: {current_stock}")
        response = requests.post(f"{GATEWAY_URL}/stock/add/{item_id}/{quantity}")
        print(f"Rollback response: {response.status_code}")
        current_stock = requests.get(f"{GATEWAY_URL}/stock/find/{item_id}").json()["stock"]
        print(f"Stock of {item_id} after rollback: {current_stock}")



consumer = RabbitMQConsumer()

if __name__ == '__main__':
    print("The number of queues is" + str(N_QUEUES))
    queues = []
    queues = [f'main_{REPLICA_INDEX}', f'test_{REPLICA_INDEX}']
    # queues = ['main', 'test']
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
