package com.synergyx.trading.service.patternApplyService;

import com.synergyx.trading.dto.patternApply.PatternApplyRequestDTO;
import com.synergyx.trading.dto.patternApply.PatternApplyResponseDTO;

public interface PatternApplyService {
    // 패턴 적용
    PatternApplyResponseDTO.PatternApplyResultDTO applyPattern(Long userId, PatternApplyRequestDTO.PatternApplyDTO request);

    // 패턴 알림 토글
    PatternApplyResponseDTO.PatternApplyResultDTO toggleNotification(Long userId, Long patternApplyId);
}
