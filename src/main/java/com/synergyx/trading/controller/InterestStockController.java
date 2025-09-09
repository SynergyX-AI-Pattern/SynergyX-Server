package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
import com.synergyx.trading.config.context.UserContext;
import com.synergyx.trading.dto.InterestStock.InterestStockResponseDTO;
import com.synergyx.trading.service.InterestStockService.InterestStockCommandService;
import com.synergyx.trading.service.InterestStockService.InterestStockQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping
@Tag(name = "관심종목 API", description = "관심종목 관련 API 입니다.")
public class InterestStockController {

    private final UserContext userContext;
    private final InterestStockCommandService interestStockCommandService;
    private final InterestStockQueryService interestStockQueryService;

    @Operation(summary = "관심종목 등록", description = "사용자의 관심종목을 등록합니다.")
    @PostMapping("/watchlist/{stockId}")
    public ResponseEntity<?> addWatchlist(
            @Parameter
            @PathVariable Long stockId) {
        Long userId = userContext.getCurrentUserId();
        interestStockCommandService.addInterest(userId, stockId);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.SUCCESS_WATCHLIST_ADD.getCode(),
                SuccessStatus.SUCCESS_WATCHLIST_ADD.getMessage()
        ));
    }

    @Operation(summary = "관심종목 해제", description = "사용자의 관심종목을 삭제합니다.")
    @DeleteMapping("/watchlist/{stockId}")
    public ResponseEntity<?> removeWatchlist(
            @Parameter
            @PathVariable Long stockId) {
        Long userId = userContext.getCurrentUserId();
        interestStockCommandService.removeInterest(userId, stockId);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.SUCCESS_WATCHLIST_REMOVE.getCode(),
                SuccessStatus.SUCCESS_WATCHLIST_REMOVE.getMessage()
        ));
    }

    @Operation(summary = "관심종목 목록 조회", description = "현재 등록된 관심종목 목록을 조회합니다.")
    @GetMapping("/watchlist")
    public ResponseEntity<?> getWatchlist() {
        Long userId = userContext.getCurrentUserId();
        List<InterestStockResponseDTO> list = interestStockQueryService.getInterestList(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(list));
    }

    @Operation(summary = "최근 조회 종목 리스트 조회", description = "최근 조회한 종목 리스트를 조회합니다. (최대 20개)")
    @GetMapping("/stocks/recent")
    public ResponseEntity<?> getRecentViewStockList() {
        Long userId = userContext.getCurrentUserId();
        List<InterestStockResponseDTO> list = interestStockQueryService.getRecentViewStocks(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(list));
    }
}