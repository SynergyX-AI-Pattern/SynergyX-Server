package com.synergyx.trading.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pattern_apply")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternApplyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 패턴 테이블 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id")
    private PatternEntity pattern;

    // 백테스팅 테이블 일대다 매핑
//    @OneToMany(mappedBy = "patternApply")
//    private List<BacktestEntity> backtestResults;

    // 임시 데이터 -> 종목 엔티티 생성 후 수정
    @Column(name = "stock_id")
    private String stockId;

    // 종목 테이블 매핑
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "stock_id")
//    private StockEntity stock;

}

