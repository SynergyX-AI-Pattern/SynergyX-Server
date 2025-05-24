package com.synergyx.trading.model;

import com.synergyx.trading.conveter.PatternPointsConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pattern")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String patternName; // 패턴 이름

    @Convert(converter = PatternPointsConverter.class)
    @Lob
    private List<Double> points; // 좌표

    private Double tolerance; // 오차 범위
    private Integer periodValue; // 기간 수치

    @Enumerated(EnumType.STRING)
    private PeriodUnit periodUnit; // 기간 단위

    @CreatedDate
    private LocalDateTime createdAt; // 생성 일시
    @LastModifiedDate
    private LocalDateTime updatedAt; // 수정 일시

    // 적용된 종목 목록 (중간 엔티티 통해)
    @OneToMany(mappedBy = "pattern")
    private List<PatternApplyEntity> patternApplies;

    // 임시 데이터 -> 유저 엔티티 생성 후 수정
    @Column(name = "user_id")
    private String userId; // 사용자 아이디

    // 사용자 테이블 매핑
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private UserEntity user;
}

