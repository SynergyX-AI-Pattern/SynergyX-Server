package com.synergyx.trading.repository;

import com.synergyx.trading.model.StockOhlcv1d;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StockOhlcv1dRepository extends JpaRepository<StockOhlcv1d, Long> {

    // 중복 방지
    Optional<StockOhlcv1d> findByStock_IdAndTimestamp(Long stockId, LocalDateTime timestamp);

    // 중복 확인
    boolean existsByStockIdAndTimestamp(Long stockId, LocalDateTime timestamp);
}
