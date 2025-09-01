package com.synergyx.trading.dto.pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class PatternResponseDTO {
    // 패턴 목록 조회
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatternDTO {
        private Long patternId; // 패턴 아이디
        private String patternName; // 패턴 이름
        private List<Double> points; // 좌표
        private List<BacktestSummaryDTO> recentBacktestResults; // 최근 백테스트 결과
    }
    // 최근 백테스트 결과 (목록 조회용)
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BacktestSummaryDTO {
        private String stockName; // 종목 이름
        private Double averageReturn; // 평균 수익률
        private Double winRate; // 승률
        private Integer matchedCount; // 패턴 매칭 횟수
        private LocalDate executedAt; // 백테스팅 실행 날짜
    }
    // 패턴 상세 조회
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatternDetailDTO {
        private Long patternId; // 패턴 아이디
        private String patternName; // 패턴 이름
        private List<Double> points; // 좌표
        private Double tolerance; // 오차 범위
        private Integer periodValue; // 기간 수치
        private String periodUnit; // 기간 단위
        private BacktestResultDTO backtestResult; // 최근 백테스트 결과
        private List<AppliedStockDTO> appliedStockList; // 패턴 적용 종목 리스트
    }
    // 백테스트 결과
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BacktestResultDTO {
        private Long backtestId; // 백테스팅 아이디
        private String symbol; // 종목 코드
        private String stockName; // 종목 이름
        private String stockImage; // 종목 이미지
        private LocalDate executedAt; // 백테스팅 실행 날짜
        private Integer matchedCount; // 패턴 매칭 횟수
        private LocalDate startDate; // 백테스트 시작일
        private LocalDate endDate; // 백테스트 종료일
        private Double winRate; // 승률
        private Double averageReturn; // 평균 수익률
        private Double maxReturn; // 최대 수익률
        private LocalDate maxReturnDate; // 최대 수익률 발생일
    }

    // 패턴 적용된 종목 리스트
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedStockDTO {
        private Long stockId; // 종목 아이디
        private String symbol; // 종목 코드
        private String stockName; // 종목 이름
        private String stockImage; // 종목 이미지
    }
}


