package wdm.payment.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {

    User user;

    @BeforeEach
    void setup(){
        user = new User();
    }

    @Test
    @DisplayName("Test user id setter and getter")
    void testGetterUserId(){
        user.setUser_id(1L);
        assertEquals(1L,user.getUser_id());
    }

    @Test
    @DisplayName("Test credit getter")
    void testGetterBookedAmount(){
        assertEquals(0,user.getCredit());
    }


    @Test
    @DisplayName("Test reserved amount setter")
    void testSetterReservedAmount(){
        user.increaseCredit(10);
        assertEquals(10,user.getCredit());
    }


    @Test
    @DisplayName("Test booked amount setter")
    void testSetterBookedAmount(){
        user.decreaseCredit(10);
        assertEquals(-10,user.getCredit());
    }
}
