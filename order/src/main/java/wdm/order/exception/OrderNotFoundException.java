package wdm.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Order not found")
public class OrderNotFoundException extends RuntimeException{
    public OrderNotFoundException(Long order_id) {
        super("order: " + order_id + " not found");
    }
}
