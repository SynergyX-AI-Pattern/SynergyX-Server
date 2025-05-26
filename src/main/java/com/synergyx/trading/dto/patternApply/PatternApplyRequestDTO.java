package com.synergyx.trading.dto.patternApply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 패턴 적용
public class PatternApplyRequestDTO {
    private Long patternId;
    private Long stockId;
}

