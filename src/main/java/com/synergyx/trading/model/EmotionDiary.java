package com.synergyx.trading.model;

import com.synergyx.trading.converter.EmotionListConverter;
import com.synergyx.trading.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "emotion_diary")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionDiary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 테이블 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 일기 원문
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    // 감정
    @Column(name = "emotion", columnDefinition = "TEXT", nullable = false)
    @Convert(converter = EmotionListConverter.class)
    private List<String> emotion;

    // 요약
    @Column(name = "summary", columnDefinition = "TEXT", nullable = false)
    private String summary;

    // 조언
    @Column(name = "feedback", columnDefinition = "TEXT", nullable = false)
    private String feedback;

}

