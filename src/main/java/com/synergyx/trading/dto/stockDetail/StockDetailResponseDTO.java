package com.synergyx.trading.dto.stockDetail;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDetailResponseDTO {
    private String stockName;
    private String price; //54,300
    private String changeRate; // 1.12%
    private String changeAmount; // 600
    private Boolean isWatchlist;
    private Boolean isTradeNotificationEnabled;
    private PredictionDTO prediction;
    private FinancialsDTO financials;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PredictionDTO {
        private String upperBound; // 상한 예측 ex.63,000
        private String lowerBound; // 하한 예측
        private String buyPrice; // 적정 매수가
        private String sellPrice; // 적정 매도가
        private String targetRange; // 예측 범위
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FinancialsDTO {
        private String pbr;
        private String per;
        private String psr;
        private String roe;
        private String marketCap;
        private String dividendYield;
    }
}