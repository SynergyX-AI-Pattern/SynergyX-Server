package com.synergyx.trading.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_ohlcv")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockOhlcv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Float open;

    @Column(nullable = false)
    private Float high;

    @Column(nullable = false)
    private Float low;

    @Column(nullable = false)
    private Float close;

    @Column(nullable = false)
    private Integer volume;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
