package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.dto.backtest.BacktestRequestDTO;
import com.synergyx.trading.dto.backtest.BacktestResponseDTO;
import com.synergyx.trading.service.backtestService.BacktestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/backtests")
public class BacktestController {
    private final BacktestService backtestService;

    // 백테스트 실행
    @PostMapping
    public ApiResponse<BacktestResponseDTO.BacktestExecutionDTO> runBacktest(
            @RequestParam Long patternId,
            @RequestParam(defaultValue = "1") Long stockId, // 종목 엔티티 생성 전 하드코딩 -> 구현 후 수정
            @RequestBody BacktestRequestDTO request) {
        BacktestResponseDTO.BacktestExecutionDTO result = backtestService.runBacktest(patternId, stockId, request);
        return ApiResponse.of(SuccessStatus._OK, result);
    }

    // 과거 백테스팅 결과 상세 조회
    @GetMapping("/results/{backtestId}")
    public ApiResponse<BacktestResponseDTO.BacktestResultDetailDTO> getBacktestResultDetail(@PathVariable Long backtestId) {
        BacktestResponseDTO.BacktestResultDetailDTO dto = backtestService.getBacktestResultDetail(backtestId);
        return ApiResponse.of(SuccessStatus._OK, dto);
    }

    // 최근 백테스팅 결과 목록 조회
    @GetMapping("/results")
    public ApiResponse<BacktestResponseDTO.BacktestResultListDTO> getBacktestResultList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BacktestResponseDTO.BacktestSummaryDTO> resultPage = backtestService.getBacktestResultList(page - 1, size);

        BacktestResponseDTO.BacktestResultListDTO dto = BacktestResponseDTO.BacktestResultListDTO.builder()
                .content(resultPage.getContent())
                .pageInfo(BacktestResponseDTO.BacktestResultListDTO.PageInfoDTO.builder()
                        .page(resultPage.getNumber() + 1)
                        .size(resultPage.getSize())
                        .totalElements(resultPage.getTotalElements())
                        .totalPages(resultPage.getTotalPages())
                        .build())
                .build();

        return ApiResponse.of(SuccessStatus._OK, dto);
    }
}
