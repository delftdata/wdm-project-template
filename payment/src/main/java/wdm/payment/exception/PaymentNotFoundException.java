package wdm.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Payment not found")
public class PaymentNotFoundException extends RuntimeException{

    public PaymentNotFoundException(Long payment_id){
        super("Payment: " + payment_id + " not found");
    }
}
