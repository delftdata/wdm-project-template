package wdm.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Payment not found")
public class PaymentNotFoundException extends RuntimeException{

    public PaymentNotFoundException(String user_id){
        super("Payment: " + user_id + " not found");
    }
}
