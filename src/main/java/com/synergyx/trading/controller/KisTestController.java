package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.service.kisService.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test/kis")
@Tag(name = "    Test API", description = "테스트 API 입니다.")
public class KisTestController {

    private final KisTokenService kisTokenService;
    private final KisRoeUpdateService kisRoeService;
    private final KisPriceUpdateService kisStockDetailService;
    private final KisPsrUpdateService kisPsrUpdateService;
    private final KisDividendScheduleService kisDividendScheduleService;
    private final KisOhlcvUpdateService kisOhlcvUpdateService;

    @Operation(summary = "KIS Access Token API", description = "KIS의 Access Token 을 발급 또는 로드합니다.")
    @GetMapping("/token")
    public ResponseEntity<String> getAccessTokenForTest() {
        String accessToken = kisTokenService.getAccessToken();
        return ResponseEntity.ok("발급된 AccessToken: " + accessToken);
    }

    @Operation(summary = "전체 Stock Detail 업데이트 API", description = "시가총액, 현재가, 등락률, per, pbr 을 가져옵니다.")
    @PostMapping("/stocks/details")
    public ResponseEntity<?> updateStockDetails() {
        try {
            kisStockDetailService.updateStockDetailsFromKis();
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "주식 디테일 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "개별 종목 Stock Detail 업데이트 API", description = "시가총액, 현재가, 등락률, per, pbr 을 가져옵니다.")
    @PostMapping("/stocks/{symbol}/details")
    public ResponseEntity<?> updateStockDetailsBySymbol(
            @Parameter(description = "종목 코드", required = true)
            @PathVariable String symbol) {
        try {
            kisStockDetailService.updateStockDetailBySymbol(symbol);
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "주식 디테일 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "전체 ROE 업데이트 API", description = "ROE 값을 가져옵니다.")
    @PostMapping("/stocks/roe")
    public ResponseEntity<?> updateRoe() {
        try {
            kisRoeService.updateRoeFromKis();
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "ROE 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "개별 종목 ROE 업데이트 API", description = "개별 종목의 ROE 값을 가져옵니다.")
    @PostMapping("/stocks/{symbol}/roe")
    public ResponseEntity<?> updateRoeBySymbol(
            @Parameter(description = "종목 코드", required = true)
            @PathVariable String symbol) {
        try {
            kisRoeService.updateRoeBySymbol(symbol);
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "ROE 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "전체 PSR 업데이트 API", description = "PSR 값을 가져옵니다.")
    @PostMapping("/stocks/psr")
    public ResponseEntity<?> updatePsr() {
        try {
            kisPsrUpdateService.updatePsr();
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "PSR 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "개별 종목 PSR 업데이트 API", description = "개별 종목의 PSR 값을 가져옵니다.")
    @PostMapping("/stocks/{symbol}/psr")
    public ResponseEntity<?> updatePsrBySymbol(
            @Parameter(description = "종목 코드", required = true)
            @PathVariable String symbol) {
        try {
            kisPsrUpdateService.updatePsrBySymbol(symbol);
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "PSR 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    // todo:수정
    @Operation(summary = "전체 배당수익률 업데이트 API", description = "배당수익률 값을 가져옵니다.")
//    @PostMapping("/stocks/div")
    public ResponseEntity<?> updateDiv() {
        try {
            kisDividendScheduleService.updateDividendYieldAll();
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "배당수익률 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    // todo:수정
    @Operation(summary = "개별 종목 배당수익률 업데이트 API", description = "개별 종목의 배당수익률 값을 가져옵니다.")
//    @PostMapping("/stocks/{symbol}/div")
    public ResponseEntity<?> updateDivBySymbol(
            @Parameter(description = "종목 코드", required = true)
            @PathVariable String symbol) {
        try {
            kisDividendScheduleService.updateDividendYieldBySymbol(symbol);
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "배당수익률 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "전체 Ohlcv 업데이트 API", description = "Ohlcv 값을 가져옵니다.")
    @PostMapping("/stocks/ohlcv")
    public ResponseEntity<?> updateOhlcv() {
        try {
            kisOhlcvUpdateService.updateOhlcvAll();
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "Ohlcv 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "개별 종목 Ohlcv 업데이트 API", description = "개별 종목의 Ohlcv 값을 가져옵니다.")
    @PostMapping("/stocks/{symbol}/ohlcv")
    public ResponseEntity<?> updateOhlcvBySymbol(
            @Parameter(description = "종목 코드", required = true)
            @PathVariable String symbol) {
        try {
            kisOhlcvUpdateService.updateOhlcvBySymbol(symbol);
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "Ohlcv 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }
}