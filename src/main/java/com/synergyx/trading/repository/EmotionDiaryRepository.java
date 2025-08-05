package com.synergyx.trading.repository;

import com.synergyx.trading.model.EmotionDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmotionDiaryRepository extends JpaRepository<EmotionDiary, Long> {
    List<EmotionDiary> findAllByUserIdOrderByCreatedAtAsc(Long userId);
}