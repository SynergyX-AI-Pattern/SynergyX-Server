package com.synergyx.trading.repository;

import com.synergyx.trading.dto.backtest.BacktestRankingDTO;
import com.synergyx.trading.model.Backtest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BacktestRankingRepository extends JpaRepository<Backtest, Long> {

    // 최대 수익률 월간 랭킹 조회
    @Query("""

            SELECT new com.synergyx.trading.dto.backtest.BacktestRankingDTO(
        b.user.id,
        b.user.username,
        b.user.image,
        b.winRate,
        b.averageReturn,
        b.maxReturn,
        b.maxReturnDate,
        b.id
    )
    FROM Backtest b
    WHERE b.executedAt BETWEEN :startOfMonth AND :endOfMonth
      AND b.user.username <> '탈퇴회원'
      AND b.maxReturn = (
          SELECT MAX(b2.maxReturn)
          FROM Backtest b2
          WHERE b2.user.id = b.user.id
            AND b2.executedAt BETWEEN :startOfMonth AND :endOfMonth
      )
    ORDER BY b.maxReturn DESC, b.winRate DESC
    """)
    List<BacktestRankingDTO> findMonthlyUserMaxReturnRankings(
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth,
            Pageable pageable
    );
}
