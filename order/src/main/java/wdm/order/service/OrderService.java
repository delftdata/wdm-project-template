package wdm.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import wdm.order.model.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class OrderService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Value("${stock.service.url}")
    private String stockServiceUrl;

    @Value("${timeout-minutes}")
    private int timeOut;

    @Async
    public CompletableFuture<Boolean> reserveStock(Order order) {
        // Retrieve the items from the order and aggregate to get count of each item
        Long order_id = order.getOrder_id();
        ArrayList<Long> items = new ArrayList<>(order.getItems());
        Map<Long, Integer> itemCounts = new HashMap<>();
        for (Long item : items) {
            itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
        }

        //Call the reserve endpoint in the other microservice for each unique item
        for (Map.Entry<Long, Integer> entry : itemCounts.entrySet()) {
            Long itemId = entry.getKey();
            int amount = entry.getValue();
            String url = stockServiceUrl + "/reserve/" + order_id + "/" + itemId + "/" + amount;

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    System.out.println("Error: " + response.getStatusCode());
                    return CompletableFuture.completedFuture(false);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                return CompletableFuture.completedFuture(false);
            }
        }
        return CompletableFuture.completedFuture(true);
    }

    @Async
    public CompletableFuture<Boolean> bookStock(Order order) {
        Long order_id = order.getOrder_id();
        ArrayList<Long> items = new ArrayList<>(order.getItems());

        // Book all items from stock
        for (Long item : items) {
            String url = stockServiceUrl + "/buy/" + order_id + "/" + item;
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

                if (response.getStatusCodeValue() != 200) {
                    System.out.println("Error: " + response.getStatusCodeValue());
                    return CompletableFuture.completedFuture(false);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                return CompletableFuture.completedFuture(false);
            }
        }
        return CompletableFuture.completedFuture(true);
    }

    @Async
    public CompletableFuture<Boolean> reservePayment(Order order) {
        Long user_id = order.getUser_id();
        Long order_id = order.getOrder_id();
        float amount = order.getTotal_cost();

        String url = paymentServiceUrl + "/reserve/" + user_id + "/" + order_id + "/" + amount;
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            if (response.getStatusCodeValue() != 200) {
                System.out.println("Error: " + response.getStatusCodeValue());
                return CompletableFuture.completedFuture(false);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.completedFuture(true);

    }

    @Async
    public CompletableFuture<Boolean> bookPayment(Order order) {
        Long order_id = order.getOrder_id();
        Long user_id = order.getUser_id();
        float amount = order.getTotal_cost();

        // Pay the order from payment
        String url = paymentServiceUrl + "/pay/" + user_id + "/" + order_id + "/" + amount;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            if (response.getStatusCodeValue() != 200) {
                System.out.println("Error: " + response.getStatusCodeValue());
                return CompletableFuture.completedFuture(false);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.completedFuture(true);
    }

    public boolean reserveOut(Order order) {
        CompletableFuture<Boolean> reserveStock = reserveStock(order);
        CompletableFuture<Boolean> reservePayment = reservePayment(order);

        //This perhaps has to be seperated out for individual rollback
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(reserveStock, reservePayment);
        try {
            allFutures.get(timeOut, TimeUnit.MINUTES);

            if (reserveStock.get() && reservePayment.get()) {
                return true;
            }
        } catch (Exception e) {
            if (!reserveStock.isCompletedExceptionally()) {
                //@TODO reserveStock rollback
            }
            if (!reservePayment.isCompletedExceptionally()) {
                //@TODO reservePayment rollback
            }
        }
        return false;
    }

    public boolean checkout(Order order) {
        CompletableFuture<Boolean> bookStock = bookStock(order);
        CompletableFuture<Boolean> bookPayment = bookPayment(order);

        //This perhaps has to be seperated out for individual rollback
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(bookStock, bookPayment);
        try {
            allFutures.get(timeOut, TimeUnit.MINUTES);
            if (bookStock.get() && bookPayment.get()) {
                return true;
            }
        } catch (Exception e) {
            if (!bookStock.isCompletedExceptionally()) {
                //@TODO bookStock rollback
            }
            if (!bookPayment.isCompletedExceptionally()) {
                //@TODO bookPayment rollback
            }
        }

        return false;
    }

}