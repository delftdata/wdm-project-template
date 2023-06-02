package wdm.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import wdm.order.model.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

    public float getItemPrice(Long item_id) throws Exception {
        String url = stockServiceUrl + "/find/" + item_id;
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            if (response.getStatusCodeValue() != 200) {
                System.out.println("Error: " + response.getStatusCodeValue());
                throw new Exception();
            }
            else {
                //Handling response as json to ensure decoupling between order and stock.
                String stock = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode stockJson = objectMapper.readValue(stock, JsonNode.class);
                return (float) stockJson.get("price").asDouble();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

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

    public boolean rollbackRStock(Order order) {
        // Retrieve the items from the order and aggregate to get count of each item
        Long order_id = order.getOrder_id();

        //Call the rollback endpoint in the other microservice for each unique item
        for (Long itemId : order.getItems()) {
            String url = stockServiceUrl + "/rollback/" + order_id + "/" + itemId;

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    System.out.println("Error: " + response.getStatusCode());
                    return false;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                return false;
            }
        }
        return true;
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

    //Example of control method for retry.
    public boolean cancelPayment(Order order) {
        try {
            return cancelPaymentRequest(order);
        }
        catch (ExhaustedRetryException e) {
            //@TODO logging for retry
            return false;
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    //Example of retry, @TODO move this to the book methods (probably via control method as books give completeablefutures back)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 10000))
    public boolean cancelPaymentRequest(Order order) throws HttpClientErrorException {
        Long order_id = order.getOrder_id();
        Long user_id = order.getUser_id();

        // Pay the order from payment
        String url = paymentServiceUrl + "/cancel/" + user_id + "/" + order_id;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
        return true;
    }

    public boolean reserveOut(Order order) throws ExecutionException, InterruptedException {
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
            if (!reserveStock.isCompletedExceptionally() && reserveStock.get()) {
                //@TODO some sort of logging or retry for failure of rollback.
                rollbackRStock(order);
            }
            if (!reservePayment.isCompletedExceptionally() && reservePayment.get()) {
                //@TODO reservePayment rollback
            }
        }
        return false;
    }

    public boolean checkout(Order order) throws ExecutionException, InterruptedException {
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
            //This get should never throw an exception
            if (!bookStock.isCompletedExceptionally() && bookStock.get()) {
                //@TODO bookStock rollback
            }
            //This get should never throw an exception
            if (!bookPayment.isCompletedExceptionally() && bookPayment.get()) {
                //@TODO some sort of logging or retry for failure of rollback.
                cancelPayment(order);
            }
        }

        return false;
    }

}