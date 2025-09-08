package com.synergyx.trading.controller;

import com.synergyx.trading.config.context.UserContext;
import com.synergyx.trading.dto.pattern.PatternRequestDTO;
import com.synergyx.trading.dto.pattern.PatternResponseDTO;
import com.synergyx.trading.service.patternService.PatternCommandService;
import com.synergyx.trading.service.patternService.PatternQueryService;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/patterns")
@Tag(name = "패턴 API", description = "패턴 관련 API 입니다.")
public class PatternController {

    private final UserContext userContext;
    private final PatternCommandService patternCommandService;
    private final PatternQueryService patternQueryService;

    // 패턴 생성
    @Operation(summary = "패턴 생성",
            description = """
                사용자의 종목 패턴을 생성합니다.<br>
                지원하는 패턴 단위: `HOUR`(1-23) / `DAY`(1-30)<br>
                총 기간이 1일이 넘어야 합니다.
                """
    )
    @PostMapping
    public ResponseEntity<?> createPattern(@RequestBody @Valid PatternRequestDTO dto) {
        Long userId = userContext.getCurrentUserId();
        PatternResponseDTO.PatternDTO createdPattern = patternCommandService.createPattern(userId, dto);

        return ResponseEntity.ok(ApiResponse.onSuccess(
                createdPattern,
                SuccessStatus.SUCCESS_PATTERN_CREATE.getCode(),
                SuccessStatus.SUCCESS_PATTERN_CREATE.getMessage()
        ));
    }

    // 패턴 목록 조회
    @Operation(summary = "패턴 목록 조회", description = "사용자의 종목 패턴 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getPatternList() {
        Long userId = userContext.getCurrentUserId();
    List<PatternResponseDTO.PatternDTO> list = patternQueryService.getPatternList(userId);
    return ResponseEntity.ok(ApiResponse.onSuccess(list));
    }

    // 패턴 상세 조회
    @Operation(summary = "패턴 상세 조회", description = "선택한 패턴의 상세 정보를 조회합니다.")
    @GetMapping("/{patternId}")
    public ResponseEntity<?> getPatternDetail(
            @Parameter
            @PathVariable Long patternId) {
        Long userId = userContext.getCurrentUserId();
        PatternResponseDTO.PatternDetailDTO dto = patternQueryService.getPatternDetail(userId, patternId);
        return ResponseEntity.ok(ApiResponse.onSuccess(dto));
    }


    // 패턴 수정
    @Operation(summary = "패턴 수정", description = "사용자의 종목 패턴을 수정합니다.")
    @PatchMapping("/{patternId}")
    public ResponseEntity<?> updatePattern(
            @PathVariable Long patternId,
            @RequestBody PatternRequestDTO dto) {
        Long userId = userContext.getCurrentUserId();
        patternCommandService.updatePattern(userId, patternId, dto);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.SUCCESS_PATTERN_UPDATE.getCode(),
                SuccessStatus.SUCCESS_PATTERN_UPDATE.getMessage()
        ));
    }

    // 패턴 삭제
    @Operation(summary = "패턴 삭제", description = "사용자의 종목 패턴을 삭제합니다.")
    @DeleteMapping("/{patternId}")
    public ResponseEntity<?> deletePattern(
            @Parameter
            @PathVariable Long patternId) {
        Long userId = userContext.getCurrentUserId();
        patternCommandService.deletePattern(userId, patternId);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.SUCCESS_PATTERN_DELETE.getCode(),
                SuccessStatus.SUCCESS_PATTERN_DELETE.getMessage()
        ));
    }
}

