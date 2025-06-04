package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.dto.patternApply.PatternApplyRequestDTO;
import com.synergyx.trading.dto.patternApply.PatternApplyResponseDTO;
import com.synergyx.trading.service.patternApplyService.PatternApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pattern-applies")
public class PatternApplyController {

    private final PatternApplyService patternApplyService;

    // 패턴 적용
    @PostMapping
    public ApiResponse<PatternApplyResponseDTO.PatternApplyResultDTO> applyPattern(
            @RequestBody PatternApplyRequestDTO.PatternApplyDTO request) {
        PatternApplyResponseDTO.PatternApplyResultDTO response =
                patternApplyService.applyPattern(request);
        return ApiResponse.of(SuccessStatus._OK, response);
    }

    // 패턴 알림 토글
    @PatchMapping("/{patternApplyId}/notification")
    public ApiResponse<PatternApplyResponseDTO.PatternApplyResultDTO> toggleNotification(
            @PathVariable Long patternApplyId) {
        PatternApplyResponseDTO.PatternApplyResultDTO response =
                patternApplyService.toggleNotification(patternApplyId);
        return ApiResponse.of(SuccessStatus._OK, response);
    }

}

