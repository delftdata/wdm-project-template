package wdm.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import wdm.payment.exception.PaymentNotFoundException;
import wdm.payment.model.User;
import wdm.payment.repository.UserRepository;

@RestController
public class PaymentController {

    private final UserRepository repository;

    public PaymentController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/find_user/{user_id}")
    User findUser(@PathVariable String user_id){
        return repository.findById(user_id).orElseThrow(()-> new PaymentNotFoundException(user_id));
    }

    @PostMapping("/create_user")
    String createUser(){
        User tmp = new User();
        repository.save(tmp);
        return tmp.getUser_id();
    }

    @PostMapping("/add_funds/{user_id}/{amount}")
    boolean addFunds(@PathVariable String user_id, @PathVariable float amount){
        boolean done = true;
        User tmp = repository.findById(user_id).orElseThrow(()-> new PaymentNotFoundException(user_id));
        tmp.increaseCredit(amount);
        repository.save(tmp);
        return done;
    }

    @GetMapping("/status/{user_id}/{order_id}")
    boolean statusPayment(@PathVariable String user_id, @PathVariable String order_id){
        repository.findById(user_id).orElseThrow(()-> new PaymentNotFoundException(user_id));
        return true;
    }

    @PostMapping("/cancel/{user_id}/{order_id}")
    void cancelPayment(@PathVariable String user_id, @PathVariable String order_id){
        repository.findById(user_id).orElseThrow(()-> new PaymentNotFoundException(user_id));
    }

    @PostMapping("/pay/{user_id}/{order_id}/{amount}")
    @ResponseStatus(value = HttpStatus.OK)
    void payPayment(@PathVariable String user_id, @PathVariable String order_id, @PathVariable float amount){
        User tmp = repository.findById(user_id).orElseThrow(()-> new PaymentNotFoundException(user_id));
        tmp.decreaseCredit(amount);
        repository.save(tmp);
    }

}
