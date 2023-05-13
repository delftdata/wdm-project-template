package wdm.payment.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    Long user_id;
    float credit;

    public User() {
        credit = 0;
    }
    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public float getCredit() {
        return credit;
    }

    public void increaseCredit(float credit) {
        this.credit += credit;
    }

    public void decreaseCredit(float credit) {
        this.credit -= credit;
    }


    @Override
    public String toString() {
        return "Payment{" +
                "user_id='" + user_id + '\'' +
                ", credit=" + credit +
                '}';
    }
}
