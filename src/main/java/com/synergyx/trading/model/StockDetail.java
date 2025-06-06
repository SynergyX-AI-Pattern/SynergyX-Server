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
    @Column(name = "stock_id")
    private Long stockId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Column(nullable = false)
    private Double price;

    @Column(name = "change_rate", nullable = false)
    private Double changeRate;

    @Column(name = "financial_data", columnDefinition = "json", nullable = false)
    private String financialData;
}
