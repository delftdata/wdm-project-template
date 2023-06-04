package wdm.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Checkout failed")
public class CheckoutFailedException extends RuntimeException{
    public CheckoutFailedException(){super("Checkout failed");}
}
