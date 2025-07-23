package com.synergyx.trading.service.patternApplyService;

import com.synergyx.trading.dto.patternApply.PatternApplyResponseDTO;

public interface PatternApplyQueryService {
    // 종목-패턴 상세 조회
    PatternApplyResponseDTO.PatternApplyDetailDTO getPatternApplyDetail(Long userId, Long stockId);
}
