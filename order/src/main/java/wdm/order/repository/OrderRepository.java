package wdm.order.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import wdm.order.model.Order;

@Repository
public interface OrderRepository extends CrudRepository<Order, String> {}
