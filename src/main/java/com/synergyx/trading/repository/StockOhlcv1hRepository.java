package com.synergyx.trading.repository;

import com.synergyx.trading.model.StockOhlcv1h;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockOhlcv1hRepository extends JpaRepository<StockOhlcv1h, Long> {

    // 중복 방지용
    Optional<StockOhlcv1h> findByStock_IdAndTimestamp(Long stockId, LocalDateTime timestamp);

    // 기간별 시간봉 조회 (오름차순 정렬)
    List<StockOhlcv1h> findByStockIdAndTimestampBetweenOrderByTimestampAsc(
            Long stockId, LocalDateTime start, LocalDateTime end
    );
}

