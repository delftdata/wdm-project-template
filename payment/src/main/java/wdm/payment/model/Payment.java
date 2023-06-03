package wdm.payment.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Payment")
public class Payment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    long payment_id;
    long userId;
    long orderId;
    float reserved_amount;
    float booked_amount;
    @Version
    private Long version;

    public Payment() {
    }

    public Payment(long userId, long orderId, float reserved_amount, float booked_amount) {
        this.userId = userId;
        this.orderId = orderId;
        this.reserved_amount = reserved_amount;
        this.booked_amount = booked_amount;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long user_id) {
        this.userId = user_id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long order_id) {
        this.orderId = order_id;
    }

    public float getReserved_amount() {
        return reserved_amount;
    }

    public void setReserved_amount(float reserved_amount) {
        this.reserved_amount = reserved_amount;
    }

    public float getBooked_amount() {
        return booked_amount;
    }

    public void setBooked_amount(float booked_amount) {
        this.booked_amount = booked_amount;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "payment_id=" + payment_id +
                ", user_id=" + userId +
                ", order_id=" + orderId +
                ", reserved_amount=" + reserved_amount +
                ", booked_amount=" + booked_amount +
                '}';
    }
}

