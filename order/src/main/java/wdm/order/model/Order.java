package wdm.order.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.ArrayList;

@RedisHash("Order")
public class Order implements Serializable {
    @Id
    String order_id;
    boolean paid;
    ArrayList<String> items;
    String user_id;
    float total_cost;

    public Order(String user_id) {
        this.paid = false;
        this.items = new ArrayList<>();
        this.user_id = user_id;
        this.total_cost = 0;
    }

    public String getOrder_id() {
        return order_id;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean addItem(String item_id) {
        return items.add(item_id);
    }

    public boolean removeItem(String item_id) {
        return items.remove(item_id);
    }

    public float getTotal_cost() {
        return total_cost;
    }

    public void setTotal_cost(float total_cost) {
        this.total_cost = total_cost;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public String getUser_id() {
        return user_id;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + order_id + '\'' +
                ", paid=" + paid +
                ", items=" + items +
                ", user='" + user_id + '\'' +
                ", cost=" + total_cost +
                '}';
    }
}
