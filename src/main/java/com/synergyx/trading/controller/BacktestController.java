package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.dto.backtest.BacktestRequestDTO;
import com.synergyx.trading.dto.backtest.BacktestResponseDTO;
import com.synergyx.trading.service.backtestService.BacktestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/backtests")
@Tag(name = "백테스팅 API", description = "백테스팅 관련 API 입니다.")
public class BacktestController {

    // 임시 userId
    private static final Long TEMP_USER_ID = 1L;

    private final BacktestService backtestService;

    // 백테스트 실행
    @Operation(summary = "백테스팅 실행", description = "백테스팅을 실행합니다.")
    @PostMapping
    public ResponseEntity<?> runBacktest(
            @RequestParam Long patternId,
            @RequestParam Long stockId,
            @RequestBody BacktestRequestDTO request) {
        BacktestResponseDTO.BacktestExecutionDTO result = backtestService.runBacktest(TEMP_USER_ID, patternId, stockId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                result,
                SuccessStatus.SUCCESS_BACKTEST_EXECUTE.getCode(),
                SuccessStatus.SUCCESS_BACKTEST_EXECUTE.getMessage()
        ));
    }

    // 과거 백테스팅 결과 상세 조회
    @Operation(summary = "백테스팅 결과 상세 조회", description = "해당 백테스팅 결과의 상세 정보를 조회합니다.")
    @GetMapping("/results/{backtestId}")
    public ResponseEntity<?>  getBacktestResultDetail(
            @Parameter
            @PathVariable Long backtestId) {
        BacktestResponseDTO.BacktestResultDetailDTO dto = backtestService.getBacktestResultDetail(TEMP_USER_ID, backtestId);
        return ResponseEntity.ok(ApiResponse.onSuccess(dto));
    }

    // 최근 백테스팅 결과 목록 조회
    @Operation(summary = "백테스팅 결과 목록 조회", description = "최근 백테스팅 결과 목록을 조회합니다.")
    @GetMapping("/results")
    public ResponseEntity<?>  getBacktestResultList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BacktestResponseDTO.BacktestSummaryDTO> resultPage = backtestService.getBacktestResultList(TEMP_USER_ID, page - 1, size);

        BacktestResponseDTO.BacktestResultListDTO dto = BacktestResponseDTO.BacktestResultListDTO.builder()
                .content(resultPage.getContent())
                .pageInfo(BacktestResponseDTO.BacktestResultListDTO.PageInfoDTO.builder()
                        .page(resultPage.getNumber() + 1)
                        .size(resultPage.getSize())
                        .totalElements(resultPage.getTotalElements())
                        .totalPages(resultPage.getTotalPages())
                        .build())
                .build();

        return ResponseEntity.ok(ApiResponse.onSuccess(dto));
    }
}
