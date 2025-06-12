package com.synergyx.trading.service.patternService;

import com.synergyx.trading.dto.pattern.PatternRequestDTO;
import com.synergyx.trading.dto.pattern.PatternResponseDTO;

public interface PatternCommandService {
    // 패턴 생성
    PatternResponseDTO.PatternDTO createPattern(Long userId, PatternRequestDTO dto);

    // 패턴 수정
    void updatePattern(Long userId, Long patternId, PatternRequestDTO dto);

    // 패턴 삭제
    void deletePattern(Long userId, Long patternId);
}
