package com.synergyx.trading.model;

import com.synergyx.trading.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(
        name = "prediction",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_stock_target_date",
                        columnNames = {"stock_id", "target_date"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "stock_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_prediction_stock")
    )
    private Stock stock;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "predicted_close", nullable = false)
    private Double predictedClose;

    @Column(name = "predicted_high", nullable = false)
    private Double predictedHigh;

    @Column(name = "predicted_low", nullable = false)
    private Double predictedLow;

    @Column(name = "recommended_sell")
    private Double recommendedSell;

    @Column(name = "recommended_buy")
    private Double recommendedBuy;
}
