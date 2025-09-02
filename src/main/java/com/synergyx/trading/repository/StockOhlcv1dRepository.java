package com.synergyx.trading.repository;

import com.synergyx.trading.model.StockOhlcv1d;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockOhlcv1dRepository extends JpaRepository<StockOhlcv1d, Long> {

    // 중복 방지
    Optional<StockOhlcv1d> findByStock_IdAndTimestamp(Long stockId, LocalDateTime timestamp);

    // 중복 확인
    boolean existsByStockIdAndTimestamp(Long stockId, LocalDateTime timestamp);

    // 상위 63개 ohlcv 조회 (약 3개월)
    List<StockOhlcv1d> findTop63ByStockIdOrderByTimestampDesc(Long stockId);

    // 기간별 일봉 조회 (오름차순 정렬)
    List<StockOhlcv1d> findByStockIdAndTimestampBetweenOrderByTimestampAsc(
            Long stockId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
