package com.synergyx.trading.repository;

import com.synergyx.trading.model.Pattern;
import com.synergyx.trading.model.PatternApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatternApplyRepository extends JpaRepository<PatternApply, Long> {
    // 패턴 적용 목록 조회
    List<PatternApply> findByUserIdAndPatternId(Long userId, Long patternId);
    // 패턴 삭제
    void deleteAllByPattern(Pattern pattern);
    // 종목 아이디로 종목-패턴 상세 조회
    Optional<PatternApply> findByUserIdAndStockId(Long userId, Long stockId);
}