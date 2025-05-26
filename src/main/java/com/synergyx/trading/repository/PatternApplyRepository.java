package com.synergyx.trading.repository;

import com.synergyx.trading.model.PatternApplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatternApplyRepository extends JpaRepository<PatternApplyEntity, Long> {
    List<PatternApplyEntity> findByPatternId(Long patternId);
}