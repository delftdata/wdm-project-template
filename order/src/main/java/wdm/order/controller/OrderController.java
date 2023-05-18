package wdm.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import wdm.order.exception.OrderNotFoundException;
import wdm.order.model.Order;
import wdm.order.repository.OrderRepository;
import wdm.order.service.OrderService;

import java.util.Collections;
import java.util.Map;

@RestController
public class OrderController {
    private final OrderRepository repository;

    private final OrderService orderService;
    
    public OrderController(OrderRepository repository, OrderService orderService) {
        this.repository = repository;
        this.orderService = orderService;
    }

    @PostMapping("/create/{user_id}")
    Map<String, Long> createOrder(@PathVariable Long user_id){
        Order tmp = new Order(user_id);
        repository.save(tmp);

        return Collections.singletonMap("order_id", tmp.getOrder_id());
    }

    @DeleteMapping("/remove/{order_id}")
    @ResponseStatus(value = HttpStatus.OK)
    void deleteOrder(@PathVariable Long order_id){
        repository.deleteById(order_id);
    }

    @GetMapping("/find/{order_id}")
    Order findOrder(@PathVariable Long order_id){
        return repository.findById(order_id).orElseThrow(()-> new OrderNotFoundException(order_id));
    }

    @PostMapping("/addItem/{order_id}/{item_id}")
    @ResponseStatus(value = HttpStatus.OK)
    void addItem(@PathVariable Long order_id, @PathVariable Long item_id){
        Order tmp = repository.findById(order_id).orElseThrow(()-> new OrderNotFoundException(order_id));
        tmp.addItem(item_id);
        repository.save(tmp);
    }

    @DeleteMapping("/removeItem/{order_id}/{item_id}")
    @ResponseStatus(value = HttpStatus.OK)
    void removeItem(@PathVariable Long order_id, @PathVariable Long item_id){
        Order tmp = repository.findById(order_id).orElseThrow(()-> new OrderNotFoundException(order_id));
        tmp.removeItem(item_id);
        repository.save(tmp);
    }

    @PostMapping("/checkout/{order_id}")
    @ResponseStatus(value = HttpStatus.OK)
    Map<String, Boolean> checkout(@PathVariable Long order_id) {
        boolean checkout = false;
        Order tmp = repository.findById(order_id).orElseThrow(()-> new OrderNotFoundException(order_id));
        Boolean reserved = orderService.reserveOut(tmp);
        if (reserved) {
            checkout = orderService.checkout(tmp);
        }
        return Collections.singletonMap("checkout_succes", checkout);
    }


}
