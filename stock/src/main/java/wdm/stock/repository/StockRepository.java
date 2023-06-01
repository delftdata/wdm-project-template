package wdm.stock.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import wdm.stock.model.Stock;

import java.util.Optional;

@Repository
public interface StockRepository extends CrudRepository<Stock, Long> {
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Stock> findById(Long id);
}

