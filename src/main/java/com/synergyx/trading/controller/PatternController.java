package com.synergyx.trading.controller;

import com.synergyx.trading.dto.pattern.PatternRequestDTO;
import com.synergyx.trading.dto.pattern.PatternResponseDTO;
import com.synergyx.trading.service.patternService.PatternService;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/patterns")
public class PatternController {

    private final PatternService patternService;

    // 패턴 등록 (임시 유저 아이디)
    @PostMapping
    public ApiResponse<PatternResponseDTO.PatternDTO> createPattern(
            @RequestBody @Valid PatternRequestDTO dto) {
        String userId = "SoominCho";  // 임시 유저 아이디 (나중에 사용자 인증 대체 예정)
        PatternResponseDTO.PatternDTO createdPattern = patternService.createPattern(userId, dto);
        return ApiResponse.of(SuccessStatus._OK, createdPattern);
    }

    // 패턴 목록 조회 (임시 유저 아이디)
    @GetMapping
    public ApiResponse<List<PatternResponseDTO.PatternDTO>> getPatternList() {
        String userId = "SoominCho"; // 임시 유저 아이디 (나중에 사용자 인증 대체 예정)
        List<PatternResponseDTO.PatternDTO> list = patternService.getPatternList(userId);
        return ApiResponse.of(SuccessStatus._OK, list);
    }

    // 패턴 상세 조회 (임시 유저 아이디)
    @GetMapping("/{patternId}")
    public ApiResponse<PatternResponseDTO.PatternDetailDTO> getPatternDetail(@PathVariable Long patternId) {
        String userId = "SoominCho"; // JWT 적용 전까지 고정
        PatternResponseDTO.PatternDetailDTO dto = patternService.getPatternDetail(patternId, userId);
        return ApiResponse.of(SuccessStatus._OK, dto);
    }

    // 패턴 수정 (임시 유저 아이디)
    @PatchMapping("/{patternId}")
    public ApiResponse<Void> updatePattern(@PathVariable Long patternId,
                                           @RequestBody PatternRequestDTO dto) {
        String userId = "SoominCho";
        patternService.updatePattern(patternId, userId, dto);
        return ApiResponse.of(SuccessStatus._OK, null);
    }

    // 패턴 삭제 (임시 유저 아이디)
    @DeleteMapping("/{patternId}")
    public ApiResponse<Void> deletePattern(@PathVariable Long patternId) {
        String userId = "SoominCho";
        patternService.deletePattern(patternId, userId);
        return ApiResponse.of(SuccessStatus._OK, null);
    }

}

