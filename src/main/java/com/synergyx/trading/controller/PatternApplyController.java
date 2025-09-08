package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.config.context.UserContext;
import com.synergyx.trading.dto.patternApply.PatternApplyRequestDTO;
import com.synergyx.trading.dto.patternApply.PatternApplyResponseDTO;
import com.synergyx.trading.service.patternApplyService.PatternApplyCommandService;
import com.synergyx.trading.service.patternApplyService.PatternApplyQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "패턴 적용 API", description = "패턴 적용 정보 관련 API 입니다.")
@RequestMapping("/pattern-applies")
public class PatternApplyController {

    private final UserContext userContext;
    private final PatternApplyCommandService patternApplyCommandService;
    private final PatternApplyQueryService patternApplyQueryService;

    // 패턴 적용
    @Operation(summary = "패턴 적용", description = "패턴을 종목에 적용합니다.")
    @PostMapping
    public ResponseEntity<?> applyPattern(
            @RequestBody PatternApplyRequestDTO.PatternApplyDTO request) {
        Long userId = userContext.getCurrentUserId();
        PatternApplyResponseDTO.PatternApplyResultDTO response =
                patternApplyCommandService.applyPattern(userId, request);
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
            @PathVariable @Valid @Positive Long patternApplyId) {
        Long userId = userContext.getCurrentUserId();
        PatternApplyResponseDTO.PatternApplyToggleDTO response =
                patternApplyCommandService.toggleNotification(userId, patternApplyId);
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
        Long userId = userContext.getCurrentUserId();
                patternApplyCommandService.updatePatternApply(userId, patternApplyId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.SUCCESS_PATTERN_APPLY_UPDATE.getCode(),
                SuccessStatus.SUCCESS_PATTERN_APPLY_UPDATE.getMessage()
        ));
    }

    // 패턴 적용 해제
    @Operation(summary = "패턴 적용 해제", description = "종목에 적용된 패턴을 해제합니다.")
    @DeleteMapping("/{patternApplyId}")
    public ResponseEntity<?> deletePatternApply(@PathVariable Long patternApplyId) {
        Long userId = userContext.getCurrentUserId();
        patternApplyCommandService.deletePatternApply(userId, patternApplyId);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.SUCCESS_PATTERN_APPLY_UNLINK.getCode(),
                SuccessStatus.SUCCESS_PATTERN_APPLY_UNLINK.getMessage()
        ));
    }

    // 종목-패턴 상세 조회
    @Operation(summary = "종목-패턴 상세 조회", description = "특정 종목에 적용된 패턴과 관련된 상세 정보를 조회합니다.")
    @GetMapping("/stocks/{stockId}")
    public ResponseEntity<?> getPatternApplyDetailByStockId(
            @PathVariable Long stockId
    ) {
        Long userId = userContext.getCurrentUserId();
        PatternApplyResponseDTO.PatternApplyDetailDTO response =
                patternApplyQueryService.getPatternApplyDetail(userId, stockId);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                response,
                SuccessStatus.SUCCESS_PATTERN_APPLY_DETAIL.getCode(),
                SuccessStatus.SUCCESS_PATTERN_APPLY_DETAIL.getMessage()
        ));
    }

}

