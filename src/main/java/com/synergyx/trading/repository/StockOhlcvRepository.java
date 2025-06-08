package com.synergyx.trading.repository;

import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockOhlcv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface StockOhlcvRepository extends JpaRepository<StockOhlcv, Long> {
    boolean existsByStockAndTimestamp(Stock stock, LocalDateTime timestamp);
}
