package com.synergyx.trading.service.patternApplyService;

import com.synergyx.trading.dto.patternApply.PatternApplyRequestDTO;
import com.synergyx.trading.dto.patternApply.PatternApplyResponseDTO;

public interface PatternApplyService {
    // 패턴 적용
    PatternApplyResponseDTO.PatternApplyResultDTO applyPattern(Long userId, PatternApplyRequestDTO.PatternApplyDTO request);

    // 패턴 알림 토글
    PatternApplyResponseDTO.PatternApplyToggleDTO toggleNotification(Long userId, Long patternApplyId);

    // 패턴 적용 정보 수정
    void updatePatternApply(Long userId, Long patternApplyId, PatternApplyRequestDTO.PatternApplyUpdateDTO request);

    // 패턴 적용 해제
    void deletePatternApply(Long userId, Long patternApplyId);
}
