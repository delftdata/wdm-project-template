package wdm.payment.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import wdm.payment.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {}
