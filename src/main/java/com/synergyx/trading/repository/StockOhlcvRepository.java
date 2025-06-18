package com.synergyx.trading.repository;

import com.synergyx.trading.model.StockOhlcv;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockOhlcvRepository extends JpaRepository<StockOhlcv, Long> {

    // ohlcv 중복 확인
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM StockOhlcv s WHERE s.stock.id = :stockId AND s.timestamp = :timestamp")
    boolean existsByStockIdAndTimestamp(@Param("stockId") Long stockId, @Param("timestamp") LocalDateTime timestamp);

    // 상위 n개 ohlcv 조회
    @Query("SELECT s FROM StockOhlcv s WHERE s.stock.id = :stockId ORDER BY s.timestamp DESC")
    List<StockOhlcv> findLastNCandlesByStock(@Param("stockId") Long stockId, Pageable pageable);
}
