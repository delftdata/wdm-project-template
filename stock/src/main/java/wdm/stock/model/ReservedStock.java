package wdm.stock.model;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name = "reserved_stock")
public class ReservedStock implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    int reservedQty;

    int bookedQty;

    long orderId;

    long item_id;

//    @ManyToOne
//    @JoinColumn(name = "item_id")
//    private Stock item;

    public ReservedStock() {
    }

    public ReservedStock(int reservedQty, int bookedQty, long orderId, long item_id) {
        this.reservedQty = reservedQty;
        this.bookedQty = bookedQty;
        this.orderId = orderId;
        this.item_id = item_id;
    }

    public long idGet() {
        return id;
    }
    public int getReservedQty() {
        return reservedQty;
    }

    public void setReservedQty(int reservedQty) {
        this.reservedQty = reservedQty;
    }

    public int getBookedQty() {
        return bookedQty;
    }

    public void setBookedQty(int bookedQty) {
        this.bookedQty = bookedQty;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

//    public Stock getItem() {
//        return item;
//    }
//
//    public void setItem(Stock stock) {
//        this.item = stock;
//    }
}
