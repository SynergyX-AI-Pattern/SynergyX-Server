package com.synergyx.trading.controller;

import com.synergyx.trading.apiPayload.ApiResponse;
import com.synergyx.trading.service.kisService.*;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/test/kis")
@Tag(name = "    Test API", description = "테스트 API 입니다.")
public class KisTestController {

    private final KisTokenService kisTokenService;
    private final KisRoeUpdateService kisRoeService;
    private final KisPriceUpdateService kisStockDetailService;
    private final KisPsrUpdateService kisPsrUpdateService;
    private final KisDividendScheduleService kisDividendScheduleService;
    private final KisOhlcvUpdateService kisOhlcvUpdateService;
    private final Kis1dOhlcvUpdateService kis1dOhlcvUpdateService;
    private final Kis1mOhlcvUpdateService kis1mOhlcvUpdateService;
    private final KisPast15mOhlcvUpdateService kisPast15mOhlcvUpdateService;

    @Hidden
    @Operation(summary = "KIS Access Token API", description = "KIS의 Access Token 을 발급 또는 로드합니다.")
    @GetMapping("/token")
    public ResponseEntity<String> getAccessTokenForTest() {
        String accessToken = kisTokenService.getAccessToken();
        return ResponseEntity.ok("발급된 AccessToken: " + accessToken);
    }

    @Operation(summary = "전체 Stock Detail 업데이트 API", description = "시가총액, 현재가, 등락폭, 등락률, per, pbr 을 가져옵니다.")
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

    @Operation(summary = "개별 종목 Stock Detail 업데이트 API", description = "시가총액, 현재가, 등락폭, 등락률, per, pbr 을 가져옵니다.")
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

    @Operation(summary = "전체 배당금 업데이트 API", description = "배당금 값을 가져옵니다.")
    @PostMapping("/stocks/div")
    public ResponseEntity<?> updateDiv() {
        try {
            kisDividendScheduleService.updateDividendAll();
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "배당금 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "개별 종목 배당금 업데이트 API", description = "개별 종목의 배당금 값을 가져옵니다.")
    @PostMapping("/stocks/{symbol}/div")
    public ResponseEntity<?> updateDivBySymbol(
            @Parameter(description = "종목 코드", required = true)
            @PathVariable String symbol) {
        try {
            kisDividendScheduleService.updateDividendBySymbol(symbol);
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "배당금 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "전체 Ohlcv 업데이트 API", description = "Ohlcv 값을 가져옵니다.")
//    @PostMapping("/stocks/ohlcv/15M") // 스케줄링 적용됨.
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
//    @PostMapping("/stocks/{symbol}/ohlcv/15M")
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

    @Operation(summary = "전체 3M Ohlcv 업데이트 API", description = "3M Ohlcv 값을 가져옵니다.")
//    @PostMapping("/stocks/ohlcv/3M")
    public ResponseEntity<?> update3mOhlcv() {
        try {
            kis1dOhlcvUpdateService.updateOhlcvAll();
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "3m Ohlcv 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "종목 구간 3M Ohlcv 업데이트 API", description = "startId ~ endId 사이 종목들의 3개월 OHLCV를 업데이트합니다.")
//    @PostMapping("/stocks/ohlcv/3M/range")
    public ResponseEntity<?> update3mOhlcvByStockIdRange(
            @Parameter(description = "시작 종목 ID", required = true)
            @RequestParam Long startId,

            @Parameter(description = "종료 종목 ID (옵션)")
            @RequestParam(required = false) Long endId
    ) {
        try {
            kis1dOhlcvUpdateService.updateOhlcvByStockIdRange(startId, endId);

            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "3m Ohlcv 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "전체 5Y Ohlcv 업데이트 API", description = "5Y Ohlcv 값을 가져옵니다.")
//    @PostMapping("/stocks/ohlcv/5Y")
    public ResponseEntity<?> update5yOhlcv() {
        try {
            kis1mOhlcvUpdateService.updateOhlcvAll();
            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "3m Ohlcv 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "종목 구간 5Y Ohlcv 업데이트 API", description = "startId ~ endId 사이 종목들의 5년 OHLCV를 업데이트합니다.")
//    @PostMapping("/stocks/ohlcv/5Y/range")
    public ResponseEntity<?> update5yOhlcvByStockIdRange(
            @Parameter(description = "시작 종목 ID", required = true)
            @RequestParam Long startId,

            @Parameter(description = "종료 종목 ID (옵션)")
            @RequestParam(required = false) Long endId
    ) {
        try {
            kis1mOhlcvUpdateService.updateOhlcvByStockIdRange(startId, endId);

            return ResponseEntity.ok(ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS", "3m Ohlcv 저장 완료"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(summary = "종목 구간 과거 1년간 15분 Ohlcv 업데이트 API", description = "startId ~ endId 사이 종목들의 1년간의 15분봉 OHLCV를 업데이트합니다.")
    @PostMapping("/stocks/ohlcv/15m/range")
    public ResponseEntity<?> updatePast15mOhlcvByStockIdRange(
            @Parameter(description = "시작 종목 ID", required = true)
            @RequestParam Long startId,

            @Parameter(description = "종료 종목 ID (미입력 시 startId와 동일)")
            @RequestParam(required = false) Long endId
    ) {
        try {
            // endId가 null이면 startId로 세팅
            if (endId == null) {
                endId = startId;
            }

            log.info("[API] 종목 구간 15m OHLCV 업데이트 호출 - startId={}, endId={}", startId, endId);

            kisPast15mOhlcvUpdateService.updateStocksByIdRange(startId, endId);

            return ResponseEntity.ok(
                    ApiResponse.onSuccess("STOCK_UPDATE_SUCCESS",
                            String.format("과거 1년치 15분봉 OHLCV 저장 완료 (ID %d ~ %d)", startId, endId))
            );
        } catch (Exception e) {
            log.error("[API] 종목 구간 15m OHLCV 업데이트 실패 - startId={}, endId={}", startId, endId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

    @Operation(
            summary = "단일 종목 당일 15분 OHLCV 테스트 API",
            description = "특정 종목 ID에 대해 오늘 하루치 1분봉을 조회하여 15분봉으로 변환 후 저장합니다. "
                    + "배치 전체가 아닌 단일 종목 테스트 용도로 사용합니다."
    )
//    @PostMapping("/stocks/ohlcv/15m/test")
    public ResponseEntity<?> testSingleStock(
            @Parameter(description = "종목 ID", required = true)
            @RequestParam Long stockId
    ) {
        try {
            log.info("[API] 단일 종목 15m OHLCV 테스트 호출 - stockId={}", stockId);

            kisPast15mOhlcvUpdateService.testSingleStockById(stockId);

            return ResponseEntity.ok(
                    ApiResponse.onSuccess(
                            "STOCK_TEST_SUCCESS",
                            String.format("종목 ID=%d 오늘 하루치 15분봉 OHLCV 저장 완료", stockId)
                    )
            );
        } catch (IllegalArgumentException e) {
            log.warn("[API] 단일 종목 테스트 실패 - 잘못된 종목 ID. stockId={}", stockId, e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.onFailure("INVALID_STOCK_ID", "존재하지 않는 종목 ID", e.getMessage()));
        } catch (Exception e) {
            log.error("[API] 단일 종목 15m OHLCV 테스트 실패 - stockId={}", stockId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.onFailure("INTERNAL_ERROR", "저장 중 오류 발생", e.getMessage()));
        }
    }

}