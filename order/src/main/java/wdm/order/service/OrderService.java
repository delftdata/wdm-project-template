package wdm.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import wdm.order.model.Order;
import wdm.order.repository.OrderRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {
    @Autowired
    private RestTemplate restTemplate = new RestTemplate();

    public boolean processStock(Order order) {
        // Retrieve the items from the order and aggregate to get count of each item
        Long order_id = order.getOrder_id();
        ArrayList<String> items = order.getItems();
        Map<String, Integer> itemCounts = new HashMap<>();
        for (String item : items) {
            itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
        }

         //Call the reserve endpoint in the other microservice for each unique item
         for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
             String itemId = entry.getKey();
             int amount = entry.getValue();
             String url = "http://localhost:8000/stock/reserve/" + order_id + "/" + itemId + "/" + amount;
             
             try {
                ResponseEntity<String> response = restTemplate.getForEntity(url ,String.class);
            
                if (response.getStatusCodeValue() != 200) {
                    System.out.println("Error: " + response.getStatusCodeValue());
                    return false;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                return false;
            }
         }
         return true;
    }

    public void processPayment(Order order) {
        Long user_id = order.getUser_id();
        Long order_id = order.getOrder_id();
        float amount = order.getTotal_cost();

        String url = "http://localhost:8000/payment/reserve/" + user_id + "/" + order_id + "/" + amount;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url ,String.class);
        
            if (response.getStatusCodeValue() != 200) {
                System.out.println("Error: " + response.getStatusCodeValue());
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
        return true;
    
    }

    public void checkout(Order order) {
        Long order_id = order.getOrder_id();
        Long user_id = order.getUser_id();
        float amount = order.getTotal_cost();
        ArrayList<String> items = order.getItems();

        // Book all items from stock
        for (item : items) {
            String url = "http://localhost:8000/stock/buy/" + order_id + "/" + item;
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(url,String.class);
            
                if (response.getStatusCodeValue()!= 200) {
                    System.out.println("Error: " + response.getStatusCodeValue());
                    return false;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                return false;
            }
        }

        // Pay the order from payment
        String url = "http://localhost:8000/payment/pay/" + user_id + "/" + order_id + "/" + amount;
        ResponseEntity<String> response = restTemplate.getForEntity(url,String.class);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url ,String.class);
        
            if (response.getStatusCodeValue() != 200) {
                System.out.println("Error: " + response.getStatusCodeValue());
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }


        tmp.setPaid(true);
        repository.save(tmp);

    }

}