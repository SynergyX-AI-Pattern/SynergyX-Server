package com.synergyx.trading.repository;

import com.synergyx.trading.model.Backtest;
import com.synergyx.trading.model.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface BacktestRepository extends JpaRepository<Backtest, Long> {
    // 패턴 별 백테스팅 결과 조회용 (최근 1개)
    // 실행 날짜 동일할 경우, 아이디 내림차순.
    Optional<Backtest> findTopByUserIdAndPatternIdOrderByExecutedAtDescIdDesc(Long userId, Long patternId);

    // 특정 패턴에 대한 모든 백테스팅 삭제
    void deleteAllByPattern(Pattern pattern);

    // 사용자의 백테스팅 목록 조회
    Page<Backtest> findByUserId(Long userId, Pageable pageable);

    // 사용자 + 종목 + 패턴 조합으로 실행한 가장 최근 백테스트 1건 조회
    Optional<Backtest> findTop1ByPatternIdAndStockIdAndUserIdOrderByExecutedAtDesc(
            Long patternId,
            Long stockId,
            Long userId
    );

    // 특정 패턴에 대한 최근 3개 백테스트 조회 (패턴 목록 조회용)
    List<Backtest> findTop3ByPatternIdAndUserIdOrderByExecutedAtDescIdDesc(Long patternId, Long userId);

    // 사용자의 백테스팅 정보 조회
    Optional<Backtest> findByIdAndUserId(Long backtestId, Long userId);
}