package wdm.payment.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentTest{

    Payment payment;

    @BeforeEach
    void setup(){
        payment = new Payment(1,1,10,20);
    }

    @Test
    @DisplayName("Test user id getter")
    void testGetterUserId(){
        assertEquals(1,payment.getUserId());
    }

    @Test
    @DisplayName("Test user id setter")
    void testSetterUserId(){
        payment.setUserId(2);
        assertEquals(2,payment.getUserId());
    }

    @Test
    @DisplayName("Test order id getter")
    void testGetterOrderId(){
        assertEquals(1,payment.getOrderId());
    }

    @Test
    @DisplayName("Test order id setter")
    void testSetterOrderId(){
        payment.setOrderId(2);
        assertEquals(2,payment.getOrderId());
    }

    @Test
    @DisplayName("Test reserved amount getter")
    void testGetterReservedAmount(){
        assertEquals(10,payment.getReserved_amount());
    }

    @Test
    @DisplayName("Test reserved amount setter")
    void testSetterReservedAmount(){
        payment.setReserved_amount(20);
        assertEquals(20,payment.getReserved_amount());
    }

    @Test
    @DisplayName("Test booked amount getter")
    void testGetterBookedAmount(){
        assertEquals(20,payment.getBooked_amount());
    }

    @Test
    @DisplayName("Test booked amount setter")
    void testSetterBookedAmount(){
        payment.setBooked_amount(10);
        assertEquals(10,payment.getBooked_amount());
    }
}
