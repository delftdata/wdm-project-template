import requests

ORDER_URL = STOCK_URL = PAYMENT_URL = "http://127.0.0.1:8000"


########################################################################################################################
#   STOCK MICROSERVICE FUNCTIONS
########################################################################################################################
def create_item(price: float) -> dict:
    return requests.post(f"{STOCK_URL}/stock/item/create/{price}").json()


def find_item(item_id: str) -> dict:
    return requests.get(f"{STOCK_URL}/stock/find/{item_id}").json()


def add_stock(item_id: str, amount: int) -> int:
    return requests.post(f"{STOCK_URL}/stock/add/{item_id}/{amount}").status_code


def subtract_stock(item_id: str, amount: int) -> int:
    return requests.post(f"{STOCK_URL}/stock/subtract/{item_id}/{amount}").status_code


########################################################################################################################
#   PAYMENT MICROSERVICE FUNCTIONS
########################################################################################################################
def payment_pay(user_id: str, order_id: str, amount: float) -> int:
    return requests.post(f"{PAYMENT_URL}/payment/pay/{user_id}/{order_id}/{amount}").status_code


def create_user() -> dict:
    return requests.post(f"{PAYMENT_URL}/payment/create_user").json()


def find_user(user_id: str) -> dict:
    return requests.get(f"{PAYMENT_URL}/payment/find_user/{user_id}").json()


def add_credit_to_user(user_id: str, amount: float) -> int:
    return requests.post(f"{PAYMENT_URL}/payment/add_funds/{user_id}/{amount}").status_code


########################################################################################################################
#   ORDER MICROSERVICE FUNCTIONS
########################################################################################################################
def create_order(user_id: str) -> dict:
    return requests.post(f"{ORDER_URL}/orders/create/{user_id}").json()


def add_item_to_order(order_id: str, item_id: str) -> int:
    return requests.post(f"{ORDER_URL}/orders/addItem/{order_id}/{item_id}").status_code


def find_order(order_id: str) -> dict:
    return requests.get(f"{ORDER_URL}/orders/find/{order_id}").json()


def checkout_order(order_id: str) -> requests.Response:
    return requests.post(f"{ORDER_URL}/orders/checkout/{order_id}")


########################################################################################################################
#   STATUS CHECKS
########################################################################################################################
def status_code_is_success(status_code: int) -> bool:
    return 200 <= status_code < 300


def status_code_is_failure(status_code: int) -> bool:
    return 400 <= status_code < 500
