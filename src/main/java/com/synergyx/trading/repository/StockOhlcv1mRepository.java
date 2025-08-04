package com.synergyx.trading.repository;

import com.synergyx.trading.model.StockOhlcv1m;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockOhlcv1mRepository extends JpaRepository<StockOhlcv1m, Long> {

    Optional<StockOhlcv1m> findByStock_IdAndTimestamp(Long stockId, LocalDateTime timestamp);

    // 상위 12개 ohlcv 조회 (1년 치)
    List<StockOhlcv1m> findTop12ByStockIdOrderByTimestampDesc(Long stockId);

    // 상위 60개 ohlcv 조회 (5년 치)
    List<StockOhlcv1m> findTop60ByStockIdOrderByTimestampDesc(Long stockId);
}
