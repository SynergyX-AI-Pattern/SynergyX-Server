package com.synergyx.trading.model;

import com.synergyx.trading.model.common.BaseEntity;
import com.synergyx.trading.model.common.Ohlcv;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stock_ohlcv_1h",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"stock_id", "timestamp"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockOhlcv1h extends BaseEntity implements Ohlcv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Double open;

    @Column(nullable = false)
    private Double high;

    @Column(nullable = false)
    private Double low;

    @Column(nullable = false)
    private Double close;

    @Column(nullable = false)
    private Long volume;

    @Version
    @Builder.Default
    @Column(nullable = false)
    private Long version = 0L;
}
