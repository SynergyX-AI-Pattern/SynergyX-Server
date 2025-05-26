package com.synergyx.trading.repository;

import com.synergyx.trading.model.BacktestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BacktestRepository extends JpaRepository<BacktestEntity, Long> {
    // 패턴 별 백테스팅 결과 조회용
    Optional<BacktestEntity> findTopByPatternIdOrderByExecutedAtDesc(Long patternId);
}