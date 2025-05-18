package com.synergyx.trading.dto.pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
        private String executedAt; // 백테스팅 실행 날짜
        private Double winRate; // 승률
        private Double averageReturn; // 평균 수익률
        private Integer matchedCount; // 패턴 매칭 횟수
    }

    // 패턴 적용된 종목 리스트
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedStockDTO {
        private String symbol; // 종목 코드
        private String stockName; // 종목 이름
        private String stockImage; // 종목 이미지
    }
}


