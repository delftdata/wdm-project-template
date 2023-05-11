package wdm.stock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Item not found")
public class StockNotFoundException extends RuntimeException{
    public StockNotFoundException(Long item_id){
        super("item: " + item_id + " not found");
    }

}
