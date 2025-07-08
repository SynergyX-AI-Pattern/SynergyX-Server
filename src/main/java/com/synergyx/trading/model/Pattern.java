package com.synergyx.trading.model;

import com.synergyx.trading.converter.PatternPointsConverter;
import com.synergyx.trading.enums.PeriodUnit;
import com.synergyx.trading.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "pattern")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pattern extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 테이블 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 적용된 종목 목록
    @OneToMany(mappedBy = "pattern")
    private List<PatternApply> patternApplies;

    @Column(name = "pattern_name", nullable = false)
    private String patternName; // 패턴 이름

    @Convert(converter = PatternPointsConverter.class)
    @Lob
    @Column(name = "points", nullable = false)
    private List<Double> points; // 좌표

    @Column(name = "tolerance", nullable = false)
    private Double tolerance; // 오차 범위

    @Column(name = "period_value", nullable = false)
    private Integer periodValue; // 기간 수치

    @Column(name = "period_unit", nullable = false)
    @Enumerated(EnumType.STRING)
    private PeriodUnit periodUnit; // 기간 단위
}

