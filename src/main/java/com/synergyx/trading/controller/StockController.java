package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.dto.stockDetail.RankedStockDTO;
import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;
import com.synergyx.trading.dto.stockDetail.StockDetailResponseDTO;
import com.synergyx.trading.dto.stockSearch.StockSearchResponseDTO;
import com.synergyx.trading.service.stockService.candle.StockCandleQueryService;
import com.synergyx.trading.service.stockService.detail.StockDetailQueryService;
import com.synergyx.trading.service.stockService.ranking.StockRankingQueryService;
import com.synergyx.trading.service.stockService.search.StockSearchQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/stocks")
@Tag(name = "종목 상세 | 홈 API", description = "종목 상세, 홈 화면 관련 API 입니다.")
public class StockController {

    // 임시 userId
    private static final Long TEMP_USER_ID = 1L;

    private final StockSearchQueryService stockSearchQueryService;
    private final StockDetailQueryService stockDetailQueryService;
    private final StockCandleQueryService stockCandleQueryService;
    private final StockRankingQueryService stockRankingQueryService;

    @Operation(summary = "종목 상세 조회", description = "종목 상세 정보를 조회합니다.")
    @GetMapping("/{stockId}/detail")
    public ResponseEntity<?> getStockDetail(@PathVariable Long stockId) {
        StockDetailResponseDTO dto = stockDetailQueryService.getStockDetail(stockId, TEMP_USER_ID);
        return ResponseEntity.ok(ApiResponse.onSuccess(dto));
    }

    @Operation(summary = "종목 검색", description = "종목명을 기준으로 종목을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<?> searchStocks(
            @Parameter(description = "검색할 종목명 (예: 삼성, 하이닉스)")
            @RequestParam String query) {
        List<StockSearchResponseDTO> result = stockSearchQueryService.searchStocksByName(query);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }

    @Operation(
            summary = "종목 캔들 데이터 조회",
            description = """
                    `interval` (1D, 1W, 3M, 1Y, 5Y)에 따라 캔들 데이터를 조회합니다.
                            
                    ⚠️ 현재 3M, 1Y, 5Y는 임시로 1W 기준 캔들(약 40~50개)을 반환하며,  
                    추후 실제 데이터 연동 시 캔들 수가 변경될 수 있습니다.
                    """
    )// todo:description 수정
    @GetMapping("/stocks/{stockId}/candles")
    public ResponseEntity<?> getStockCandles(
            @PathVariable Long stockId,
            @Parameter(
                    name = "interval",
                    description = "캔들 구간 (1D, 1W, 3M, 1Y, 5Y 중 하나 입력)",
                    required = true
            )
            @RequestParam String interval) {

        List<StockCandleResponseDTO> candles = stockCandleQueryService.getCandles(stockId, interval);

        return ResponseEntity.ok(ApiResponse.onSuccess(
                candles,
                SuccessStatus.SUCCESS_CHART_DATA.getCode(),
                SuccessStatus.SUCCESS_CHART_DATA.getMessage()
        ));
    }

    @Operation(summary = "TOP 20 종목 조회", description = "TOP 20 종목을 조회합니다. (거래대금 기준)")
    @GetMapping("/top20")
    public ResponseEntity<?> getTop20Stocks() {
        List<RankedStockDTO> list = stockRankingQueryService.getTop20();
        return ResponseEntity.ok(ApiResponse.onSuccess(list));
    }

    @Operation(summary = "AI TOP 20 종목 조회", description = "AI TOP 20 종목을 조회합니다. (상승폭 기준)")
    @GetMapping("/ai-top20")
    public ResponseEntity<?> getAiTop20Stocks() {
        List<RankedStockDTO> list = stockRankingQueryService.getAiTop20();
        return ResponseEntity.ok(ApiResponse.onSuccess(list));
    }
}