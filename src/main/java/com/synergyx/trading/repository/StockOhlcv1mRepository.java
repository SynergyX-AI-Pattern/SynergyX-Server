package com.synergyx.trading.repository;

import com.synergyx.trading.model.StockOhlcv1m;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StockOhlcv1mRepository extends JpaRepository<StockOhlcv1m, Long> {

    Optional<StockOhlcv1m> findByStock_IdAndTimestamp(Long stockId, LocalDateTime timestamp);
}
