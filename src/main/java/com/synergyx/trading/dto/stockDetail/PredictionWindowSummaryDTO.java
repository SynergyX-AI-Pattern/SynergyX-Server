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
    private long upperForecast;
    private long lowerForecast;
    private long fairSell;
    private long fairBuy;
    private double bufferPct;
}
