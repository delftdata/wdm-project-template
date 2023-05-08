package wdm.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Insufficient funds")
public class InsufficientCreditException extends RuntimeException{

    public InsufficientCreditException(float credit, float required) {
        super("User credit: " + credit + ", Credit required: " + required);
    }
}

