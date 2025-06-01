package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.dto.InterestStock.InterestStockResponseDTO;
import com.synergyx.trading.service.InterestStockService.InterestStockCommandService;
import com.synergyx.trading.service.InterestStockService.InterestStockQueryService;
import io.swagger.v3.oas.annotations.Operation;
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

    private final InterestStockCommandService interestStockCommandService;
    private final InterestStockQueryService interestStockQueryService;

    @Operation(summary = "관심종목 등록", description = "사용자의 관심종목을 등록합니다.")
    @PostMapping
    public ResponseEntity<?> addWatchlist(
            @PathVariable Long stockId,
            @RequestParam Long userId) {
        interestStockCommandService.addInterest(userId, stockId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Operation(summary = "관심종목 해제", description = "사용자의 관심종목을 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<?> deleteWatchlist(
            @PathVariable Long stockId,
            @RequestParam Long userId) {
        interestStockCommandService.removeInterest(userId, stockId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Operation(summary = "관심종목 목록 조회", description = "현재 등록된 관심종목 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<?> getWatchlist(@RequestParam Long userId) {
        List<InterestStockResponseDTO> list = interestStockQueryService.getInterestList(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(list));
    }
}

