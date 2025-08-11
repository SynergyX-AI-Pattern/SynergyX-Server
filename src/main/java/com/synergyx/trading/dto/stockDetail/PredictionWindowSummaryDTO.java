package com.synergyx.trading.dto.stockDetail;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
// 내부 계산용
public class PredictionWindowSummaryDTO {
    private Long stockId;
    private LocalDate asOfDate;
    private int windowDays;
    private double upperForecast;
    private double lowerForecast;
    private double fairSell;
    private double fairBuy;
    private double bufferPct;
    private LocalDateTime expiresAt;
}
