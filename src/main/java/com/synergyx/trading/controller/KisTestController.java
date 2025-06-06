package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.service.kisService.KisCommandService;
import com.synergyx.trading.service.kisService.KisTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/kis")
@Tag(name = "Test API", description = "테스트 API 입니다.")
public class KisTestController {

    private final KisTokenService kisTokenService;
    private final KisCommandService kisCommandService;

    @Operation(summary = "KIS Access Token API", description = "KIS의 Access Token 을 발급 또는 로드합니다.")
    @GetMapping("/token")
    public ResponseEntity<String> getAccessTokenForTest() {
        String accessToken = kisTokenService.getAccessToken();
        return ResponseEntity.ok("✅ 발급된 AccessToken: " + accessToken);
    }

    @Operation(summary = "Stock Detail 업데이트 API", description = "시가총액, 현재가, 등락률, per, pbr 을 가져옵니다.")
    @PostMapping("/update-stock_detail")
    public ResponseEntity<?> updateStockDetails() {
        try {
            kisCommandService.updateStockDetailsFromKis();
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "주식 디테일 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", null));
        }
    }

    @Operation(summary = "ROE 업데이트 API", description = "ROE 값을 가져옵니다.")
    @PostMapping("/update-roe")
    public ResponseEntity<?> updateRoe() {
        try {
            kisCommandService.updateRoeFromKis();
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "ROE 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", null));
        }
    }

    @Operation(summary = "개별 종목 ROE 업데이트 API", description = "개별 종목의 ROE 값을 가져옵니다.")
    @PostMapping("/update-specific-roe")
    public ResponseEntity<?> updateSpecificRoe(@RequestParam String stockCode) {
        try {
            kisCommandService.updateRoeFromKisTest(stockCode);
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "ROE 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", null));
        }
    }
}