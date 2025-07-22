package com.synergyx.trading.repository;

import com.synergyx.trading.model.StockOhlcv;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockOhlcvRepository extends JpaRepository<StockOhlcv, Long> {

    // ohlcv 중복 확인
    boolean existsByStockIdAndTimestamp(Long stockId, LocalDateTime timestamp);

    // 상위 n개 ohlcv 조회 (1D)
    @Query("SELECT s FROM StockOhlcv s WHERE s.stock.id = :stockId ORDER BY s.timestamp DESC")
    List<StockOhlcv> findLastNCandlesByStock(@Param("stockId") Long stockId, Pageable pageable);

    // 지정 시점 이후의 15분봉 캔들 데이터 조회 (1W, 3M, 1Y, 5Y)
    List<StockOhlcv> findByStockIdAndTimestampAfter(Long stockId, LocalDateTime from);

    // 거래대금 기반 랭킹 정렬 시 사용할 최신 timestamp 조회
    @Query("SELECT MAX(o.timestamp) FROM StockOhlcv o")
    LocalDateTime findLatestTimestamp();

    // timestamp 기준 거래대금 높은 순 정렬
    @Query("""
                SELECT o FROM StockOhlcv o
                JOIN FETCH o.stock
                WHERE o.timestamp = :latest
                ORDER BY (o.close * o.volume) DESC
            """)
    List<StockOhlcv> findTopByTimestamp(@Param("latest") LocalDateTime latest, Pageable pageable);

    // 기간별 조회 (정렬 포함)
    List<StockOhlcv> findByStockIdAndTimestampBetweenOrderByTimestampAsc(Long stockId, LocalDateTime start, LocalDateTime end);

    // entryAt (포함) 이전의 가장 최근의 종가 조회
    Optional<StockOhlcv> findTop1ByStockIdAndTimestampLessThanEqualOrderByTimestampDesc(Long stockId, LocalDateTime entryAt);

}
