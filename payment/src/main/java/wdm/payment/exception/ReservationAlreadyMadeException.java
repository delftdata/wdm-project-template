package wdm.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Reservation already existed")
public class ReservationAlreadyMadeException extends RuntimeException{

    public ReservationAlreadyMadeException(long user_id, long order_id){
        super("Reservation already existed for order: " + order_id + " of user: " + user_id);
    }
}
