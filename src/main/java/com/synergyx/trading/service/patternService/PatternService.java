package com.synergyx.trading.service.patternService;

import com.synergyx.trading.dto.pattern.PatternRequestDTO;
import com.synergyx.trading.dto.pattern.PatternResponseDTO;

import java.util.List;

public interface PatternService {
    // 패턴 생성 (임시 유저)
    PatternResponseDTO.PatternDTO createPattern(String userId, PatternRequestDTO dto);

    // 패턴 목록 조회 (임시 유저)
    List<PatternResponseDTO.PatternDTO> getPatternList(String userId);

    // 패턴 상세 조회 (임시 유저)
    PatternResponseDTO.PatternDetailDTO getPatternDetail(Long patternId, String userId);

    // 패턴 수정 (임시 유저)
    void updatePattern(Long patternId, String userId, PatternRequestDTO dto);

    // 패턴 삭제 (임시 유저)
    void deletePattern(Long patternId, String userId);
}
