package com.synergyx.trading.dto.patternApply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PatternApplyResponseDTO {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    // [패턴 상세 화면] 적용된 종목 목록
    public static class PatternApplyListDTO {
        private Long applyId;
        private String patternName;
        private String stockSymbol;
        private String stockName;
    }
    // 패턴 적용
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatternApplyResultDTO {
        private Long patternApplyId;
        private Boolean isAlertEnabled;
        private LocalDateTime entryAt;
        private Double entryPrice;
    }
    // 알림 토글
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatternApplyToggleDTO  {
        private Long patternApplyId;
        private Boolean isAlertEnabled;
    }
    // [관심 종목 상세 화면] 종목 정보, 패턴 정보, 최근 백테스팅 결과
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatternApplyDetailDTO {
        private Long patternApplyId;

        // 종목 정보
        private StockDTO stock;

        // 패턴 정보
        private PatternDTO pattern;

        // 최근 백테스트 결과
        private BacktestResultDTO backtestResult;

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StockDTO {
            private Long stockId;
            private String stockName;
            private String stockImage;
        }
        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PatternDTO {
            private Long patternId;
            private List<Double> points;
            private Double tolerance;
            private Integer periodValue;
            private String periodUnit;
        }
        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class BacktestResultDTO {
            private Long backtestId;
            private LocalDate executedAt;
            private Integer matchedCount;
            private LocalDate startDate;
            private LocalDate endDate;
            private Double winRate;
            private Double averageReturn;
            private Double maxReturn;
            private LocalDate maxReturnDate;
        }
    }
}

