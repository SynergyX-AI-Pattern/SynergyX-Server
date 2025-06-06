package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.service.kisService.KisCommandService;
import com.synergyx.trading.service.kisService.KisTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "Update Stock Detail API", description = "시가총액, 현재가, 등락율, per, pbr을 가져옵니다.")
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
}