package wdm.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import wdm.payment.exception.InsufficientCreditException;
import wdm.payment.exception.ReservationAlreadyMadeException;
import wdm.payment.exception.ReservationNeverMadeException;
import wdm.payment.exception.UserNotFoundException;
import wdm.payment.model.Payment;
import wdm.payment.model.User;
import wdm.payment.repository.PaymentRepository;
import wdm.payment.repository.UserRepository;

@Service
@Transactional
public class PaymentService {

    UserRepository userrepository;
    PaymentRepository paymentRepository;

    public PaymentService(UserRepository userrepository, PaymentRepository paymentRepository) {
        this.userrepository = userrepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void reserveCredit(long user_id, long order_id, float amount){
        User user = userrepository.findById(user_id).orElseThrow(()-> new UserNotFoundException(user_id));
        if(paymentRepository.existsByUserIdAndOrderId(user_id, order_id)) throw new ReservationAlreadyMadeException(user_id, order_id);
        if(user.getCredit() >= amount){
            Payment newPayment = new Payment(user_id, order_id, amount, 0);
            user.decreaseCredit(amount);
            userrepository.save(user);
            paymentRepository.save(newPayment);
        } else {
            throw new InsufficientCreditException(user.getCredit(), amount);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void bookCredit(long user_id, long order_id, float amount){
        Payment payment = paymentRepository.findByUserIdAndOrderId(user_id, order_id).orElseThrow(() -> new ReservationNeverMadeException(user_id, order_id, amount));
        if(payment.getBooked_amount() == amount && payment.getReserved_amount() == 0){
            return;
        } else if (payment.getReserved_amount() == amount && payment.getBooked_amount() == 0) {
            payment.setBooked_amount(amount);
            payment.setReserved_amount(0);
            paymentRepository.save(payment);
            return;
        }
        throw new RuntimeException("Error occured with booking amount: " +amount+" for user: " + user_id + " and order: " + order_id);

    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void cancelBooking(long user_id, long order_id){
        Payment payment = paymentRepository.findByUserIdAndOrderId(user_id, order_id).orElseThrow(() -> new RuntimeException("Error occured with cancelling the booking for user: " + user_id + " and order: " + order_id));
        if(payment.getBooked_amount() > 0 && payment.getReserved_amount() == 0){
            payment.setReserved_amount(payment.getBooked_amount());
            payment.setBooked_amount(0);
            paymentRepository.save(payment);
            return;
        } else if (payment.getReserved_amount() > 0 && payment.getBooked_amount() == 0) {
            return;
        }
        throw new RuntimeException("Error occured with cancelling the booking for user: " + user_id + " and order: " + order_id);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean checkStatusPayment(long user_id, long order_id) {
        Payment payment = paymentRepository.findByUserIdAndOrderId(user_id, order_id).orElseThrow(()-> new RuntimeException("Error occured with checking the booking for user: " + user_id + " and order: " + order_id));
        return payment.getBooked_amount() > 0 && payment.getReserved_amount() == 0;
    }

}
