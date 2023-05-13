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



    public void processOrder(Order order) {
        // Retrieve the items from the order and aggregate to get count of each item
        ArrayList<String> items = order.getItems();
        Map<String, Integer> itemCounts = new HashMap<>();
        for (String item : items) {
            itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
        }

         //Call the reserve endpoint in the other microservice for each unique item
         for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
             String itemId = entry.getKey();
             int amount = entry.getValue();
             String url = "http://localhost:8000/stock/find/" + itemId;
             ResponseEntity<String> response = restTemplate.getForEntity(url ,String.class);

             if (response.getStatusCodeValue() != 200) {
                System.out.println("Error: " + response.getStatusCodeValue());
             }
         }
    }
}