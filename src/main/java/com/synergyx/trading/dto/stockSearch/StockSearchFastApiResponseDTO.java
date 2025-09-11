package com.synergyx.trading.dto.stockSearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.synergyx.trading.enums.StockStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StockSearchFastApiResponseDTO  {

    // FastAPI에서 받은 응답을 감싸는 Wrapper DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockSearchFastApiWrapperDTO {

        // camelCase로 매핑
        @JsonProperty("is_success")
        private boolean isSuccess;

        private String code;
        private String message;
        private StockSearchFastApiInfoResponseDTO data;
    }

    // 반환 응답
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockSearchFastApiInfoResponseDTO {
        private Long id; // stock id
        private String name; // 종목명
        private String imageUrl; // 종목 이미지
        private StockStatus status; // 종목 상태
    }
}
