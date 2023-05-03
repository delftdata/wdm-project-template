package wdm.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Item not in order")
public class ItemNotInOrderException extends RuntimeException{
    public ItemNotInOrderException(String order_id, String item_id) {
        super("order: " + order_id + "does not contain item: " + item_id);
    }
}