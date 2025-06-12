package com.synergyx.trading.repository;

import com.synergyx.trading.model.Backtest;
import com.synergyx.trading.model.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface BacktestRepository extends JpaRepository<Backtest, Long> {
    // 패턴 별 백테스팅 결과 조회용 (최근 1개)
    Optional<Backtest> findTopByUserIdAndPatternIdOrderByExecutedAtDesc(Long userId, Long patternId);
    void deleteAllByPattern(Pattern pattern);
    Page<Backtest> findByUserId(Long userId, Pageable pageable);
}