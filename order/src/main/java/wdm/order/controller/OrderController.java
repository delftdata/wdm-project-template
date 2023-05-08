package wdm.order.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import wdm.order.exception.OrderNotFoundException;
import wdm.order.model.Order;
import wdm.order.repository.OrderRepository;

@RestController
public class OrderController {

    @Value("${order.gateway.url}")
    private String gatewayUrl;

    private final OrderRepository repository;

    public OrderController(OrderRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/create/{user_id}")
    String createOrder(@PathVariable String user_id){
        Order tmp = new Order(user_id);
        repository.save(tmp);
        return tmp.getOrder_id();
    }

    @DeleteMapping("/remove/{order_id}")
    @ResponseStatus(value = HttpStatus.OK)
    void deleteOrder(@PathVariable String order_id){
        repository.deleteById(order_id);
    }

    @GetMapping("/find/{order_id}")
    Order findOrder(@PathVariable String order_id){
        return repository.findById(order_id).orElseThrow(()-> new OrderNotFoundException(order_id));
    }

    @PostMapping("/addItem/{order_id}/{item_id}")
    @ResponseStatus(value = HttpStatus.OK)
    void addItem(@PathVariable String order_id, @PathVariable String item_id){
        Order tmp = repository.findById(order_id).orElseThrow(()-> new OrderNotFoundException(order_id));
        tmp.addItem(item_id);
        repository.save(tmp);
    }

    @DeleteMapping("/removeItem/{order_id}/{item_id}")
    @ResponseStatus(value = HttpStatus.OK)
    void removeItem(@PathVariable String order_id, @PathVariable String item_id){
        Order tmp = repository.findById(order_id).orElseThrow(()-> new OrderNotFoundException(order_id));
        tmp.removeItem(item_id);
        repository.save(tmp);
    }

    @PostMapping("/checkout/{order_id}")
    @ResponseStatus(value = HttpStatus.OK)
    void checkout(@PathVariable String order_id){
        Order tmp = repository.findById(order_id).orElseThrow(()-> new OrderNotFoundException(order_id));
        //@TODO call payment service for payment

        //@TODO call stock service for stock update

        tmp.setPaid(true);
        repository.save(tmp);

    }


}
