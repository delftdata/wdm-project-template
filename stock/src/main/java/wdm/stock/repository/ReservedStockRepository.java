package wdm.stock.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import wdm.stock.model.ReservedStock;

@Repository
public interface ReservedStockRepository extends CrudRepository<ReservedStock, Long> {
}
