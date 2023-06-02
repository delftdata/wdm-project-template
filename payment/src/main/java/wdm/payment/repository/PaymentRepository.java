package wdm.payment.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import wdm.payment.model.Payment;

import java.util.Optional;

public interface PaymentRepository extends CrudRepository<Payment, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    public Optional<Payment> findByUserIdAndOrderId(long userId, long orderId);
    public boolean existsByUserIdAndOrderId(long user_id, long order_id);
}

