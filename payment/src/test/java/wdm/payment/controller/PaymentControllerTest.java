package wdm.payment.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import wdm.payment.model.User;
import wdm.payment.repository.UserRepository;
import wdm.payment.service.PaymentService;
import static org.mockito.ArgumentMatchers.*;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @MockBean
    UserRepository userRepository;

    @MockBean
    PaymentService paymentService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void create_user() throws Exception{
        User user = new User();
        user.setUser_id(1L);
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        mockMvc.perform(post("/create_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id", Matchers.is(1)));
    }

}
