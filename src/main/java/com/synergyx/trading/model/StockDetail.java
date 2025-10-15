package com.synergyx.trading.model;

import com.synergyx.trading.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false, unique = true)
    private Stock stock;

    @Column
    private Double price;

    @Column(name = "change_amount")
    private Double changeAmount;

    @Column(name = "change_rate")
    private Double changeRate;

    @Column(name = "financial_data", columnDefinition = "json")
    private String financialData;

    @Version
    @Column(name = "version")
    private Long version; // 동시성 제어를 위해 추가

    @Column(name = "ai_avg_increase", columnDefinition = "double default 0")
    private Double aiAvgIncrease;

    @Column(name = "ai_rank")
    private Integer aiRank;
}
