package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockOhlcv;
import com.synergyx.trading.repository.StockOhlcvRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.service.kisService.client.KisApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.synergyx.trading.util.ParsingUtil.toDouble;
import static com.synergyx.trading.util.ParsingUtil.toLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class KisOhlcvUpdateService {

    private final KisApiClient kisApiClient;
    private final ObjectMapper objectMapper;
    private final StockRepository stockRepository;
    private final StockOhlcvRepository stockOhlcvRepository;

    /**
     * 전체 종목의 OHLCV 분봉 데이터를 업데이트합니다.
     */
    @Transactional
    public void updateOhlcvAll() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            try {
                updateOhlcvInternal(stock);
                Thread.sleep(200); // API 제한 방지
            } catch (Exception e) {
                log.error("[OHLCV] {} 업데이트 실패: {}", stock.getSymbol(), e.getMessage(), e);
            }
        }
    }

    /**
     * 특정 종목의 OHLCV 분봉 데이터를 업데이트합니다.
     *
     * @param symbol 종목 코드
     */
    @Transactional
    public void updateOhlcvBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("해당 종목 없음: " + symbol));

        try {
            updateOhlcvInternal(stock);
        } catch (Exception e) {
            log.error("[OHLCV] {} 업데이트 실패: {}", symbol, e.getMessage(), e);
        }
    }

    /**
     * 종목의 OHLCV 데이터를 갱신합니다.
     *
     * @param stock 종목
     */
    private void updateOhlcvInternal(Stock stock) {
        JsonNode candles = fetchOhlcvCandles(stock.getSymbol());

        log.info("[OHLCV] 응답 전체 JSON ({}):\n{}", stock.getSymbol(),
                candles.toPrettyString());

        if (!candles.isArray()) {
            log.warn("[OHLCV] 응답 포맷 오류 - symbol: {}", stock.getSymbol());
            return;
        }

        parseAndSaveOhlcv(candles, stock);
        log.info("[OHLCV] {} 종목 분봉 데이터 저장 완료", stock.getSymbol());
    }

    /**
     * 특정 종목의 주식당일분봉조회 데이터를 요청합니다.\
     * KIS inquire-time-itemchartprice API (TR_ID: FHKST03010200) 호출.
     *
     * @param symbol 종목 코드
     * @return 캔들 JSON 배열
     */
    private JsonNode fetchOhlcvCandles(String symbol) {
        JsonNode response = kisApiClient.get(
                "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice",
                Map.of(
                        "fid_cond_mrkt_div_code", "J",
                        "fid_input_iscd", symbol,
                        "fid_input_hour_1", "000000",
                        "fid_etc_cls_code", "",
                        "fid_pw_data_incu_yn", "Y"
                ),
                "FHKST03010200"
        );

        return response.path("output2");
    }

    /**
     * 1분봉 캔들 데이터를 파싱하고 DB에 저장합니다.
     *
     * @param candles 캔들 배열
     * @param stock   종목
     */
    private void parseAndSaveOhlcv(JsonNode candles, Stock stock) {
        for (JsonNode candle : candles) {
            try {
                String date = candle.path("stck_bsop_date").asText();
                String hour = candle.path("stck_cntg_hour").asText();
                LocalDateTime timestamp = LocalDateTime.parse(date + hour, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

                if (stockOhlcvRepository.existsByStockAndTimestamp(stock, timestamp)) continue;

                // 체결량 없는 봉은 저장하지 않음
                Long volume = toLong(candle.path("cntg_vol").asText());
                if (volume == 0) continue;

                Double open = toDouble(candle.path("stck_oprc").asText());
                Double high = toDouble(candle.path("stck_hgpr").asText());
                Double low = toDouble(candle.path("stck_lwpr").asText());
                Double close = toDouble(candle.path("stck_prpr").asText());

                if (open == null || high == null || low == null || close == null || volume == null) continue;

                StockOhlcv ohlcv = StockOhlcv.builder()
                        .stock(stock)
                        .timestamp(timestamp)
                        .open(open)
                        .high(high)
                        .low(low)
                        .close(close)
                        .volume(volume)
                        .build();

                stockOhlcvRepository.save(ohlcv);
            } catch (Exception e) {
                log.error("[OHLCV] 1분봉 저장 실패 - symbol: {}, 에러: {}", stock.getSymbol(), e.getMessage(), e);
            }
        }
    }

    /**
     * 캔들 데이터를 파싱하고 DB에 저장합니다. (15분봉)
     *
     * @param candles 캔들 배열
     * @param stock   종목
     */
    private void parseAndSaveOhlcvFt(JsonNode candles, Stock stock) {
        Map<LocalDateTime, List<JsonNode>> grouped = new TreeMap<>();

        // 1. 15분 단위로 그룹핑
        for (JsonNode candle : candles) {
            try {
                String date = candle.path("stck_bsop_date").asText();
                String hour = candle.path("stck_cntg_hour").asText();
                LocalDateTime timestamp = LocalDateTime.parse(date + hour, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

                // 15분 단위로 내림
                int minute = (timestamp.getMinute() / 15) * 15;
                LocalDateTime roundedTime = timestamp.withMinute(minute).withSecond(0).withNano(0);

                grouped.computeIfAbsent(roundedTime, k -> new ArrayList<>()).add(candle);
            } catch (Exception e) {
                log.warn("[OHLCV] 캔들 시간 파싱 실패: {}", e.getMessage());
            }
        }

        // 2. 그룹별로 OHLCV 생성 및 저장
        for (Map.Entry<LocalDateTime, List<JsonNode>> entry : grouped.entrySet()) {
            LocalDateTime timestamp = entry.getKey();
            List<JsonNode> group = entry.getValue();

            try {
                if (stockOhlcvRepository.existsByStockAndTimestamp(stock, timestamp)) continue;

                // 정렬
                group.sort(Comparator.comparing(n -> {
                    String d = n.path("stck_bsop_date").asText();
                    String h = n.path("stck_cntg_hour").asText();
                    return LocalDateTime.parse(d + h, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                }));

                // open/close 계산
                JsonNode first = group.get(0);
                JsonNode last = group.get(group.size() - 1);

                Double open = toDouble(first.path("stck_oprc").asText());
                Double close = toDouble(last.path("stck_prpr").asText());
                Double high = group.stream().map(n -> toDouble(n.path("stck_hgpr").asText())).filter(Objects::nonNull).max(Double::compare).orElse(null);
                Double low = group.stream().map(n -> toDouble(n.path("stck_lwpr").asText())).filter(Objects::nonNull).min(Double::compare).orElse(null);
                Long volume = group.stream().map(n -> toLong(n.path("cntg_vol").asText())).filter(Objects::nonNull).reduce(0L, Long::sum);

                if (open == null || close == null || high == null || low == null || volume == null) continue;

                StockOhlcv ohlcv = StockOhlcv.builder()
                        .stock(stock)
                        .timestamp(timestamp)
                        .open(open)
                        .high(high)
                        .low(low)
                        .close(close)
                        .volume(volume)
                        .build();

                stockOhlcvRepository.save(ohlcv);
            } catch (Exception e) {
                log.error("[OHLCV] 15분봉 저장 실패 - symbol: {}, timestamp: {}, 에러: {}", stock.getSymbol(), timestamp, e.getMessage(), e);
            }
        }
    }
}