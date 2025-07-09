package com.synergyx.trading.repository;

import com.synergyx.trading.model.StockOhlcv1h;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StockOhlcv1hRepository extends JpaRepository<StockOhlcv1h, Long> {

    // 중복 방지용
    Optional<StockOhlcv1h> findByStock_IdAndTimestamp(Long stockId, LocalDateTime timestamp);
}

