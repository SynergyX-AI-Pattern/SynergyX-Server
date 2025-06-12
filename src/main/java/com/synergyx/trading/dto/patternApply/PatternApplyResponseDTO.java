package com.synergyx.trading.dto.patternApply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PatternApplyResponseDTO {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    // 패턴 상세 화면 -> 적용된 패턴 목록
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
    }
}

