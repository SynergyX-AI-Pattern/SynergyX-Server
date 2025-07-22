package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.dto.patternApply.PatternApplyRequestDTO;
import com.synergyx.trading.dto.patternApply.PatternApplyResponseDTO;
import com.synergyx.trading.service.patternApplyService.PatternApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "패턴 적용 API", description = "패턴 적용 정보 관련 API 입니다.")
@RequestMapping("/pattern-applies")
public class PatternApplyController {

    // 임시 userId
    private static final Long TEMP_USER_ID = 1L;

    private final PatternApplyService patternApplyService;

    // 패턴 적용
    @Operation(summary = "패턴 적용", description = "패턴을 종목에 적용합니다.")
    @PostMapping
    public ResponseEntity<?> applyPattern(
            @RequestBody PatternApplyRequestDTO.PatternApplyDTO request) {
        PatternApplyResponseDTO.PatternApplyResultDTO response =
                patternApplyService.applyPattern(TEMP_USER_ID, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                response,
                SuccessStatus.SUCCESS_PATTERN_APPLY.getCode(),
                SuccessStatus.SUCCESS_PATTERN_APPLY.getMessage()
        ));
    }

    // 패턴 알림 토글
    @Operation(summary = "패턴 알림 토글", description = "패턴 알림을 토글합니다.")
    @PatchMapping("/{patternApplyId}/notification")
    public ResponseEntity<?>toggleNotification(
            @PathVariable Long patternApplyId) {
        PatternApplyResponseDTO.PatternApplyToggleDTO response =
                patternApplyService.toggleNotification(TEMP_USER_ID, patternApplyId);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                response,
                SuccessStatus.SUCCESS_PATTERN_NOTIFICATION_TOGGLE.getCode(),
                SuccessStatus.SUCCESS_PATTERN_NOTIFICATION_TOGGLE.getMessage()
        ));
    }

    // 패턴 적용 정보 수정
    @Operation(summary = "패턴 적용 정보 수정", description = "패턴 적용 정보를 수정합니다.")
    @PatchMapping("/{patternApplyId}")
    public ResponseEntity<?> updatePatternApply(
            @PathVariable Long patternApplyId,
            @RequestBody PatternApplyRequestDTO.PatternApplyUpdateDTO request) {
                patternApplyService.updatePatternApply(TEMP_USER_ID, patternApplyId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.SUCCESS_PATTERN_APPLY_UPDATE.getCode(),
                SuccessStatus.SUCCESS_PATTERN_APPLY_UPDATE.getMessage()
        ));
    }

    // 패턴 적용 해제
    @Operation(summary = "패턴 적용 해제", description = "종목에 적용된 패턴을 해제합니다.")
    @DeleteMapping("/{patternApplyId}")
    public ResponseEntity<?> deletePatternApply(@PathVariable Long patternApplyId) {
        patternApplyService.deletePatternApply(TEMP_USER_ID, patternApplyId);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.SUCCESS_PATTERN_APPLY_UNLINK.getCode(),
                SuccessStatus.SUCCESS_PATTERN_APPLY_UNLINK.getMessage()
        ));
    }
}

