package com.synergyx.trading.repository;

import com.synergyx.trading.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    @Query("""
                SELECT MAX(p.predictedClose) AS upperForecast,
                       MIN(p.predictedClose) AS lowerForecast
                FROM Prediction p
                WHERE p.stock.id = :stockId
                  AND p.targetDate BETWEEN :startDate AND :endDate
                GROUP BY p.stock.id
            """)
    Optional<PredictionWindowAggProjection> aggregateWindow(
            @Param("stockId") Long stockId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = """
    SELECT p.stock_id AS stockId,
           s.symbol AS stockCode,
           p.target_date AS targetDate,
           p.predicted_close AS predictedClose,
           o.close AS actualClose,
           ROUND(ABS(p.predicted_close - o.close) / o.close * 100, 2) AS errorPct
    FROM prediction p
    JOIN stock s ON p.stock_id = s.id
    JOIN stock_ohlcv_1d o
      ON o.stock_id = p.stock_id
     AND DATE(o.timestamp) = p.target_date
    WHERE o.timestamp <= NOW()
    ORDER BY s.symbol, p.target_date
    """, nativeQuery = true)
    List<Object[]> findPredictionErrorsRaw();
}