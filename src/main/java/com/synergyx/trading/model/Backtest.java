package com.synergyx.trading.model;

import com.synergyx.trading.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "backtest")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Backtest extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 패턴 테이블 매핑
    @ManyToOne
    @JoinColumn(name = "pattern_id", nullable = false)
    private Pattern pattern;

    // 종목 테이블 매핑
    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // 사용자 테이블 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate executedAt; // 백테스트 실행 날짜

    @Column(nullable = false)
    private Integer matchedCount; // 패턴 매칭 횟수

    @Column(nullable = false)
    private LocalDate startDate; // 백테스트 시작일

    @Column(nullable = false)
    private LocalDate endDate; // 백테스트 종료일

    @Column(nullable = false)
    private Double winRate; // 승률

    @Column(nullable = false)
    private Double averageReturn; // 평균 수익률

    @Column(nullable = false)
    private Double maxReturn; // 최대 수익률

    @Column(nullable = false)
    private LocalDate maxReturnDate; // 최대 수익률 발생일

    @Column(nullable = false)
    private Double minReturn; // 최대 손실률

    @Column(nullable = false)
    private LocalDate minReturnDate; // 최대 손실률 발생일

    @Column(nullable = false)
    private Double totalReturn; // 모든 매칭 결과 누적 수익률

    @Column(nullable = false)
    private LocalDate lastMatchedDate; // 마지막 패턴 발생일

    @Column(nullable = false)
    private Double lastMatchedReturn; // 마지막 패턴 발생시 수익률
}
