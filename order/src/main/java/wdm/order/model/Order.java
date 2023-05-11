package wdm.order.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.ArrayList;

@Entity
@Table(name = "Orders")
public class Order implements Serializable {
    @Id
    String order_id;
    boolean paid;
    @ElementCollection
    ArrayList<String> items;
    String user_id;
    float total_cost;

    public Order() {

    }
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
