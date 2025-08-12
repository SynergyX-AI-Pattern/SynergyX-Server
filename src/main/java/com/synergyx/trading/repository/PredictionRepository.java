package com.synergyx.trading.repository;

import com.synergyx.trading.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
}