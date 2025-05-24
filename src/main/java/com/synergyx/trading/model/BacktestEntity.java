package com.synergyx.trading.model;

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
public class BacktestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 패턴 테이블 매핑
    @ManyToOne
    @JoinColumn(name = "pattern_id", nullable = false)
    private PatternEntity pattern;

    // 임시 데이터 -> 종목 엔티티 생성 후 수정
    @Column(name = "stock_id")
    private Long stockId; // 종목 아이디

    // 종목 엔티티 구현 후 캡션 제거
    // 종목 테이블 매핑
//    @ManyToOne
//    @JoinColumn(name = "stock_id", nullable = false)
//    private StockEntity stock;

    private LocalDate executedAt; // 백테스트 실행 날짜
    private Integer matchedCount; // 패턴 매칭 횟수
    private LocalDate startDate; // 백테스트 시작일
    private LocalDate endDate; // 백테스트 종료일
    private Double winRate; // 승률
    private Double averageReturn; // 평균 수익률
    private Double maxReturn; // 최대 수익률
    private LocalDate maxReturnDate; // 최대 수익률 발생일
    private Double minReturn; // 최대 손실률
    private LocalDate minReturnDate; // 최대 손실률 발생일
    private Double totalReturn; // 모든 매칭 결과 누적 수익률
    private LocalDate lastMatchedDate; // 마지막 패턴 발생일
    private Double lastMatchedReturn; // 마지막 패턴 발생시 수익률
}
