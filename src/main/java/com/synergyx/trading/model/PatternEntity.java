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

    //    임시 데이터 -> 나중에 종목/유저 엔티티 생성 후 수정
    private String symbol; // 종목 심볼
    private String stockId; // 종목 아이디
    private String userId; // 사용자 아이디

//    // 종목 테이블 매핑
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "stockId")
//    private StockEntity stock;

//    // 사용자 테이블 매핑
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "userId")
//    private UserEntity user;
}

