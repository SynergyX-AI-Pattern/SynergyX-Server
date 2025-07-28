package com.synergyx.trading.dto.patternApply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class PatternApplyRequestDTO {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    // 패턴 적용
    public static class PatternApplyDTO {
        private Long patternId; // 패턴 아이디
        private Long stockId; // 종목 아이디
        private LocalDateTime entryAt; // 진입 시점 (감지 시작일)
        private Double minValidReturn; // 최소 수익률 조건 (%)
    }
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    // 패턴 적용 정보 수정
    public static class PatternApplyUpdateDTO {
        private LocalDateTime entryAt; // 진입 시점
        private Double minValidReturn; // 최소 수익률 조건 (%)
    }
}

