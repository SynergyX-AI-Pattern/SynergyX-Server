package com.synergyx.trading.repository;

import com.synergyx.trading.model.PatternEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatternRepository extends JpaRepository<PatternEntity, String> {
    // 임시 유저
    List<PatternEntity> findByUserId(String userId);

    Optional<PatternEntity> findById(Long id);
}