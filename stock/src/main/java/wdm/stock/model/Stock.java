package wdm.stock.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.io.Serializable;

@Entity
public class Stock implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    Long item_id;
    int stock;
    float price;

    public Stock(int stock, float price) {
        this.stock = stock;
        this.price = price;
    }

    public Stock() {

    }

    public Long getItem_id() {
        return item_id;
    }

    public void setItem_id(Long item_id) {
        this.item_id = item_id;
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
                "id='" + item_id + '\'' +
                ", stock=" + stock +
                ", price=" + price +
                '}';
    }
}
