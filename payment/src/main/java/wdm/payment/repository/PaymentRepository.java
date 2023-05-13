package wdm.payment.repository;

import org.springframework.data.repository.CrudRepository;
import wdm.payment.model.Payment;

import java.util.Optional;

public interface PaymentRepository extends CrudRepository<Payment, Long> {

    public Optional<Payment> findByUserIdAndOrderId(long userId, long orderId);
    public boolean existsByUserIdAndOrderId(long user_id, long order_id);
}

