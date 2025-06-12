package com.synergyx.trading.repository;

import com.synergyx.trading.model.Pattern;
import com.synergyx.trading.model.PatternApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatternApplyRepository extends JpaRepository<PatternApply, Long> {
    List<PatternApply> findByUserIdAndPatternId(Long userId, Long patternId);
    void deleteAllByPattern(Pattern pattern);
}