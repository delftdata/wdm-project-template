package wdm.stock.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import wdm.stock.model.Stock;

@Repository
public interface StockRepository extends CrudRepository<Stock, String> {}
