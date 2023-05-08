package wdm.stock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Limit of available stock exceeded")
public class StockLimitReachedException extends RuntimeException{
    public StockLimitReachedException(int stockLimit, int requested){
        super("Stock available: " + stockLimit + ", Stock requested: " + requested);
    }

}
