package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.config.context.UserContext;
import com.synergyx.trading.dto.backtest.BacktestRequestDTO;
import com.synergyx.trading.dto.backtest.BacktestResponseDTO;
import com.synergyx.trading.service.backtestService.BacktestService;
import com.synergyx.trading.service.backtestService.ranking.BacktestRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/backtests")
@Tag(name = "백테스팅 API", description = "백테스팅 관련 API 입니다.")
public class BacktestController {

    private final UserContext userContext;
    private final BacktestService backtestService;
    private final BacktestRankingService backtestRankingService;

    // 백테스트 실행
    @Operation(summary = "백테스팅 실행", description = "백테스팅을 실행합니다.")
    @PostMapping
    public ResponseEntity<?> runBacktest(
            @RequestParam Long patternId,
            @RequestParam Long stockId,
            @RequestBody BacktestRequestDTO request) {
        Long userId = userContext.getCurrentUserId();
        BacktestResponseDTO.BacktestExecutionDTO result = backtestService.runBacktest(userId, patternId, stockId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                result,
                SuccessStatus.SUCCESS_BACKTEST_EXECUTE.getCode(),
                SuccessStatus.SUCCESS_BACKTEST_EXECUTE.getMessage()
        ));
    }

    // 과거 백테스팅 결과 상세 조회
    @Operation(summary = "백테스팅 결과 상세 조회", description = "해당 백테스팅 결과의 상세 정보를 조회합니다.")
    @GetMapping("/results/{backtestId}")
    public ResponseEntity<?> getBacktestResultDetail(
            @Parameter
            @PathVariable Long backtestId) {
        Long userId = userContext.getCurrentUserId();
        BacktestResponseDTO.BacktestResultDetailDTO dto = backtestService.getBacktestResultDetail(userId, backtestId);
        return ResponseEntity.ok(ApiResponse.onSuccess(dto));
    }

    // 최근 백테스팅 결과 목록 조회
    @Operation(summary = "백테스팅 결과 목록 조회", description = "최근 백테스팅 결과 목록을 조회합니다.")
    @GetMapping("/results")
    public ResponseEntity<?> getBacktestResultList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = userContext.getCurrentUserId();
        Page<BacktestResponseDTO.BacktestSummaryDTO> resultPage = backtestService.getBacktestResultList(userId, page - 1, size);

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

    // 백테스트 결과 차트 조회
    @Operation(summary = "백테스팅 결과 차트 조회", description = "해당 백테스팅 결과의 상세 화면에 나타낼 캔들 데이터를 조회합니다. (조회 구간: 최대 수익률 구간, 마진은 선택 사항)")
    @GetMapping("/results/{backtestId}/candles")
    public ResponseEntity<?> getBacktestCandles(
            @Parameter(description = "백테스트 ID")
            @PathVariable Long backtestId,
            @Parameter(
                    description = "기간의 앞뒤 여유 간격(기본값: 20, 최소: 0)"
            )
            @RequestParam(defaultValue = "20") int margin
    ) {
        // 마진 입력값 검증
        if (margin < 0) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }
        Long userId = userContext.getCurrentUserId();
        var candles = backtestService.getBacktestResultCandles(userId, backtestId, margin);

        return ResponseEntity.ok(ApiResponse.onSuccess(
                candles,
                SuccessStatus.SUCCESS_CHART_DATA.getCode(),
                SuccessStatus.SUCCESS_CHART_DATA.getMessage()
        ));
    }

    // 백테스팅 랭킹 조회
    @Operation(summary = "백테스팅 랭킹 조회", description = "최대 수익률을 기준으로 백테스팅 랭킹을 조회합니다. (조건: 점 3개 이상의 패턴으로 실행된 백테스팅)")
    @GetMapping("/rankings")
    public ResponseEntity<?> getRankings(
            @RequestParam(required = false, defaultValue = "100") int limit) {
        List<?> rankings = backtestRankingService.getRanking(limit);

        return ResponseEntity.ok(ApiResponse.onSuccess(
                rankings,
                SuccessStatus.SUCCESS_RANKING.getCode(),
                SuccessStatus.SUCCESS_RANKING.getMessage()
        ));
    }
}
