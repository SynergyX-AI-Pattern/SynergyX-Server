package com.synergyx.trading.dto.backtest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

public class BacktestResponseDTO  {

    // FastAPI에서 받은 응답을 감싸는 Wrapper DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BacktestWrapperResponseDTO {

        // camelCase로 매핑
        @JsonProperty("is_success")
        private boolean isSuccess;

        private String code;
        private String message;
        private BacktestExecutionDTO data;
    }

    // 백테스트 실행 결과
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BacktestExecutionDTO {
        private Long backtestId; // 백테스트 ID
        private String stockName; // 종목명
        private LocalDate executedAt; // 백테스트 실행 날짜
        private Integer matchedCount; // 패턴 매칭 횟수
        private LocalDate startDate; // 백테스트 시작일
        private LocalDate endDate; // 백테스트 종료일
        private Double winRate; // 승률
        private Double averageReturn; // 평균 수익률
        private Double maxReturn; // 최대 수익률
        private LocalDate maxReturnDate; // 최대 수익률 발생일
        private Double minReturn; // 최대 손실률
        private LocalDate minReturnDate; // 최대 손실률 발생일
        private Double totalReturn; // 모든 매칭 결과 누적 수익률
        private LocalDate lastMatchedDate; // 마지막 패턴 발생일
        private Double lastMatchedReturn; // 마지막 패턴 발생시 수익률
    }
    // 백테스팅 결과 상세 조회
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BacktestResultDetailDTO {
        private Long backtestId; // 백테스트 ID
        private String stockName; // 종목명
        private String stockImage; // 종목 이미지
        private LocalDate executedAt; // 백테스트 실행 날짜
        private Integer matchedCount; // 패턴 매칭 횟수
        private LocalDate startDate; // 백테스트 시작일
        private LocalDate endDate; // 백테스트 종료일
        private Double winRate; // 승률
        private Double averageReturn; // 평균 수익률
        private Double maxReturn; // 최대 수익률
        private LocalDate maxReturnDate; // 최대 수익률 발생일
        private Double minReturn; // 최대 손실률
        private LocalDate minReturnDate; // 최대 손실률 발생일
        private Double totalReturn; // 모든 매칭 결과 누적 수익률
        private LocalDate lastMatchedDate; // 마지막 패턴 발생일
        private Double lastMatchedReturn; // 마지막 패턴 발생시 수익률
    }

    // 백테스팅 결과 목록 조회용 content
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BacktestSummaryDTO {
        private Long backtestId; // 백테스트 ID
        private String stockName; // 종목명
        private LocalDate executedAt; // 실행 날짜
        private Double winRate; // 승률
        private Double averageReturn; // 평균 수익률
        private Integer matchedCount; // 패턴 매칭 횟수
    }

    // 백테스팅 결과 목록 조회
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BacktestResultListDTO {
        private List<BacktestSummaryDTO> content; // 백테스팅 결과 요약
        private PageInfoDTO pageInfo; // 페이지 정보

        // 페이지 정보
        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PageInfoDTO {
            private int page; // 현재 페이지 번호
            private int size; // 한 페이지 당 데이터 개수
            private long totalElements; // 전체 데이터 개수
            private int totalPages; // 전체 페이지 수
        }
    }

}
