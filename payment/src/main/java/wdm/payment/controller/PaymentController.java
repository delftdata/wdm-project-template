package wdm.payment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import wdm.payment.exception.InsufficientCreditException;
import wdm.payment.exception.PaymentNotFoundException;
import wdm.payment.exception.UserNotFoundException;
import wdm.payment.model.User;
import wdm.payment.repository.UserRepository;
import wdm.payment.service.PaymentService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Collections;
import java.util.Map;

@RestController
public class PaymentController {

    private final UserRepository repository;

    @Value("${order.gateway.url}")
    private String gatewayUrl;

    PaymentService paymentService;

    public PaymentController(UserRepository repository, PaymentService paymentService) {
        this.repository = repository;
        this.paymentService = paymentService;
    }

    @GetMapping("/find_user/{user_id}")
    User findUser(@PathVariable Long user_id){
        return repository.findById(user_id).orElseThrow(()-> new UserNotFoundException(user_id));
    }

    @PostMapping("/create_user")
    Map<String,Long> createUser(){
        User tmp = new User();
        repository.save(tmp);
        return Collections.singletonMap("user_id", tmp.getUser_id());
    }

    @PostMapping("/add_funds/{user_id}/{amount}")
    Map<String,Boolean> addFunds(@PathVariable Long user_id, @PathVariable float amount){
        boolean done = true;
        User tmp = repository.findById(user_id).orElseThrow(()-> new UserNotFoundException(user_id));
        tmp.increaseCredit(amount);
        repository.save(tmp);
        return Collections.singletonMap("done", done);
    }

    @GetMapping("/status/{user_id}/{order_id}")
    Map<String,Boolean> statusPayment(@PathVariable Long user_id, @PathVariable Long order_id) throws IOException {
        repository.findById(user_id).orElseThrow(()-> new UserNotFoundException(user_id));
        URL url = new URL(gatewayUrl + "/find/" + order_id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode order = objectMapper.readValue(response.toString(), JsonNode.class);
        return Collections.singletonMap("paid", order.get("paid").asBoolean());
    }

    @PostMapping("/cancel/{user_id}/{order_id}")
    void cancelPayment(@PathVariable long user_id, @PathVariable long order_id){
       paymentService.cancelBooking(user_id,order_id);
    }

    @PostMapping("/reserve/{user_id}/{order_id}/{amount}")
    @ResponseStatus(value = HttpStatus.OK)
    void reservePayment(@PathVariable long user_id, @PathVariable long order_id, @PathVariable float amount){
        paymentService.reserveCredit(user_id, order_id, amount);
    }

    @PostMapping("/pay/{user_id}/{order_id}/{amount}")
    @ResponseStatus(value = HttpStatus.OK)
    void payPayment(@PathVariable long user_id, @PathVariable long order_id, @PathVariable float amount){
        paymentService.bookCredit(user_id, order_id, amount);
    }


}
