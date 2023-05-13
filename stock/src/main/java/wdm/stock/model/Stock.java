package wdm.stock.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "stock")
public class Stock implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    int qty;
    float price;

//    @OneToMany(mappedBy = "item")
//    private List<ReservedStock> reservedStockList = new ArrayList<>();


    public Stock(int qty, float price) {
        this.qty = qty;
        this.price = price;
    }

    public Stock() {
    }

    public long idGet() {
        return id;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id='" + id + '\'' +
                ", qty=" + qty +
                ", price=" + price +
                '}';
    }
}
