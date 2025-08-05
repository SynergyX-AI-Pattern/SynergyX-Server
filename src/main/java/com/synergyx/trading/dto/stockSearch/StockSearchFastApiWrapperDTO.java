package com.synergyx.trading.dto.stockSearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// FastAPI에서 받은 응답을 감싸는 Wrapper DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSearchFastApiWrapperDTO {

    @JsonProperty("is_success")
    private boolean isSuccess;

    private String code;
    private String message;

    private StockSearchResponseDTO data;
}
