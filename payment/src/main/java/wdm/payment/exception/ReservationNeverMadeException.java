package wdm.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Reservation never made")
public class ReservationNeverMadeException extends RuntimeException{

    public ReservationNeverMadeException(long user_id, long order_id, float amount){
        super("Reservation of " + amount + " for order: " + order_id + " of user: " + user_id + " was never created");
    }
}
