package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.apiPayload.code.status.SuccessStatus;
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
@RequestMapping("/watchlist")
@Tag(name = "관심종목 API", description = "관심종목 관련 API 입니다.")
public class InterestStockController {

    // 임시 userId
    private static final Long TEMP_USER_ID = 1L;

    private final InterestStockCommandService interestStockCommandService;
    private final InterestStockQueryService interestStockQueryService;

    @Operation(summary = "관심종목 등록", description = "사용자의 관심종목을 등록합니다.")
    @PostMapping("/{stockId}")
    public ResponseEntity<?> addWatchlist(
            @Parameter
            @PathVariable Long stockId) {
        interestStockCommandService.addInterest(TEMP_USER_ID, stockId);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.SUCCESS_WATCHLIST_ADD.getCode(),
                SuccessStatus.SUCCESS_WATCHLIST_ADD.getMessage()
        ));
    }

    @Operation(summary = "관심종목 해제", description = "사용자의 관심종목을 삭제합니다.")
    @DeleteMapping("/{stockId}")
    public ResponseEntity<?> removeWatchlist(
            @Parameter
            @PathVariable Long stockId) {
        interestStockCommandService.removeInterest(TEMP_USER_ID, stockId);
        return ResponseEntity.ok(ApiResponse.onSuccess(
                SuccessStatus.SUCCESS_WATCHLIST_REMOVE.getCode(),
                SuccessStatus.SUCCESS_WATCHLIST_REMOVE.getMessage()
        ));
    }

    @Operation(summary = "관심종목 목록 조회", description = "현재 등록된 관심종목 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getWatchlist() {
        List<InterestStockResponseDTO> list = interestStockQueryService.getInterestList(TEMP_USER_ID);
        return ResponseEntity.ok(ApiResponse.onSuccess(list));
    }
}