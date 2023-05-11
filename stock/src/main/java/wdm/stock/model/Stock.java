package wdm.stock.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.io.Serializable;

@Entity
public class Stock implements Serializable {
    @Id
    String id;
    int stock;
    float price;

    public Stock(int stock, float price) {
        this.stock = stock;
        this.price = price;
    }

    public Stock() {

    }

    public String idGet() {
        return id;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public float getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "stock{" +
                "id='" + id + '\'' +
                ", stock=" + stock +
                ", price=" + price +
                '}';
    }
}
