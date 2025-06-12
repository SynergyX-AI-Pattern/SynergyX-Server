package com.synergyx.trading.service.patternService;
import com.synergyx.trading.dto.pattern.PatternResponseDTO;

import java.util.List;

public interface PatternQueryService {
    // 패턴 목록 조회
    List<PatternResponseDTO.PatternDTO> getPatternList(Long userId);

    // 패턴 상세 조회
    PatternResponseDTO.PatternDetailDTO getPatternDetail(Long userId, Long patternId);
}
