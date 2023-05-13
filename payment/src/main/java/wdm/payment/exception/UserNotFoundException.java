package wdm.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "User not found")
public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(Long user_id){
        super("User: " + user_id + " not found");
    }
}
