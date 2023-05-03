package wdm.stock.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import javax.annotation.processing.Generated;
import java.io.Serializable;

@RedisHash("Stock")
public class Stock implements Serializable {
    @Id
    String id;
    int stock;
    float price;

    public Stock(int stock, float price) {
        this.stock = stock;
        this.price = price;
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
