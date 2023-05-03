package wdm.payment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("User")
public class User {

    @Id
    String user_id;
    float credit;

    public User() {
        credit = 0;
    }
    public String getUser_id() {
        return user_id;
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
