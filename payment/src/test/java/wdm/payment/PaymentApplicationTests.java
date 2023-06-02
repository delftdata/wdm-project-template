package wdm.payment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import wdm.payment.controller.PaymentController;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PaymentApplicationTests {

    private final PaymentController paymentController;

    @Autowired
    public PaymentApplicationTests(PaymentController paymentController){
        this.paymentController = paymentController;
    }


	@Test
	void contextLoads() {
        Assertions.assertThat(paymentController).isNotNull();
	}

}
