package wdm.stock.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import wdm.stock.exception.StockNotFoundException;
import wdm.stock.model.Stock;
import wdm.stock.repository.ReservedStockRepository;
import wdm.stock.repository.StockRepository;

import java.sql.ResultSet;

@Service
public class StockService {
    private final JdbcTemplate jdbcTemplate;

    public StockService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void addStock(Stock stock, int quantity) {
        long itemId = stock.idGet();
        try {
            String addStockQuery = "UPDATE stock SET qty = qty + ? WHERE id = ?";
            jdbcTemplate.update(addStockQuery, quantity, itemId);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while adding stock: " + e.getMessage());
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void subtractStock(Stock stock, int quantity) {
        long itemId = stock.idGet();
        try {
            String addStockQuery = "UPDATE stock SET qty = qty - ? WHERE id = ? AND qty >= ?";
            jdbcTemplate.update(addStockQuery, quantity, itemId, quantity);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while subtracting stock: " + e.getMessage());
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void reserveStock(Stock stock, Long orderId, int quantity) {
        long itemId = stock.idGet();
        // Phase 1: Reserve stock for the order
        try {
            String decrementStockQuery = "UPDATE stock SET qty = qty - ? WHERE id = ? AND qty >= ?";
            int reserved = jdbcTemplate.update(decrementStockQuery, quantity, itemId, quantity);

            if (reserved == 0) {
                throw new StockNotFoundException(itemId);
            } else {
                String markReservedQuery = "INSERT INTO reserved_stock(id, reserved_qty, booked_qty, order_id, item_id) VALUES(DEFAULT, ?, ?, ?, ?)";
                jdbcTemplate.update(markReservedQuery, quantity, 0, orderId, itemId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while reserving stock: " + e.getMessage());
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void bookStock(Stock stock, Long orderId) {
        long itemId = stock.idGet();
        // Phase 2: Book reserved stock for the order
        try {
            String bookStockQuery = "UPDATE reserved_stock SET reserved_qty = 0, booked_qty = reserved_qty WHERE item_id = ? AND order_id = ?";
            int booked = jdbcTemplate.update(bookStockQuery, itemId, orderId);

            if (booked == 0) {
                throw new StockNotFoundException(itemId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while booking stock: " + e.getMessage());
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void rollBackStock(Stock stock, Long orderId) {
        long itemId = stock.idGet();
        try {
            String rollbackReservedQuery = "DELETE FROM reserved_stock WHERE item_id = ? AND order_id = ? RETURNING reserved_qty";
            Integer reservedQty = jdbcTemplate.execute(rollbackReservedQuery, (PreparedStatementCallback<Integer>) ps -> {
                ps.setLong(1, itemId);
                ps.setLong(2, orderId);
                boolean hasResult = ps.execute();
                int quantity = 0;
                if (hasResult) {
                    ResultSet rs = ps.getResultSet();
                    if (rs.next()) {
                        quantity = rs.getInt("reserved_qty");
                    }
                }
                return quantity;
            });

            if (reservedQty == null || reservedQty == 0) {
                throw new StockNotFoundException(itemId);
            } else {
                String updateStockQuery = "UPDATE stock SET qty = qty + ? WHERE id = ?";
                jdbcTemplate.update(updateStockQuery, reservedQty, itemId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while rolling back stock: " + e.getMessage());
        }
    }
}
