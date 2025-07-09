package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockOhlcv;
import com.synergyx.trading.model.StockOhlcv1d;
import com.synergyx.trading.repository.StockOhlcv1dRepository;
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
    private final StockOhlcv1dRepository stockOhlcv1dRepository;

    private static final int REQUEST_INTERVAL_MILLIS = 200;

    /**
     * 전체 종목의 OHLCV 분봉 데이터를 업데이트합니다.
     */
    @Transactional
    public void updateOhlcvAll() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            try {
                updateOhlcvInternal(stock);
                Thread.sleep(REQUEST_INTERVAL_MILLIS); // API 호출 제한
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
    @Transactional
    public void updateOhlcvInternal(Stock stock) {
        JsonNode candles = fetchOhlcvCandles(stock.getSymbol());

        log.info("[OHLCV] 응답 전체 JSON ({}):\n{}", stock.getSymbol(), candles.toPrettyString());

        if (!candles.isArray()) {
            log.warn("[OHLCV] 응답 포맷 오류 - symbol: {}", stock.getSymbol());
            return;
        }

        parseAndSaveOhlcv15Min(candles, stock);
        log.info("[OHLCV] {} 종목 분봉 데이터 저장 완료", stock.getSymbol());
    }

    /**
     * 특정 종목의 주식당일분봉조회 데이터를 요청합니다.
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
     * 캔들 데이터를 파싱하고 DB에 저장합니다. (15분봉)
     *
     * @param candles 캔들 배열
     * @param stock   종목
     */
    private void parseAndSaveOhlcv15Min(JsonNode candles, Stock stock) {
        Map<LocalDateTime, List<JsonNode>> grouped = new TreeMap<>();

        for (JsonNode candle : candles) {
            String date = candle.path("stck_bsop_date").asText();
            String hour = candle.path("stck_cntg_hour").asText();
            LocalDateTime timestamp = LocalDateTime.parse(date + hour, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            int minute = (timestamp.getMinute() / 15) * 15;
            LocalDateTime rounded = timestamp.withMinute(minute).withSecond(0).withNano(0);

            grouped.computeIfAbsent(rounded, k -> new ArrayList<>()).add(candle);
        }

        for (var entry : grouped.entrySet()) {
            LocalDateTime timestamp = entry.getKey();
            List<JsonNode> group = entry.getValue();

            if (stockOhlcvRepository.existsByStockIdAndTimestamp(stock.getId(), timestamp)) continue;

            group.sort(Comparator.comparing(n -> LocalDateTime.parse(
                    n.path("stck_bsop_date").asText() + n.path("stck_cntg_hour").asText(),
                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            )));

            var first = group.get(0);
            var last = group.get(group.size() - 1);

            Double open = toDouble(first.path("stck_oprc").asText());
            Double close = toDouble(last.path("stck_prpr").asText());
            Double high = group.stream().map(n -> toDouble(n.path("stck_hgpr").asText())).max(Double::compare).orElse(null);
            Double low = group.stream().map(n -> toDouble(n.path("stck_lwpr").asText())).min(Double::compare).orElse(null);
            Long volume = group.stream().map(n -> toLong(n.path("cntg_vol").asText())).reduce(0L, Long::sum);

            if (open == null || close == null || high == null || low == null || volume == null || volume == 0) continue;

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
        }
    }

    /**
     * 1분봉 캔들 데이터를 파싱하고 DB에 저장합니다. 사용 x
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

                if (stockOhlcvRepository.existsByStockIdAndTimestamp(stock.getId(), timestamp)) continue;

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
     * 15분봉 데이터를 기반으로 1일봉을 생성합니다.
     * - 장 시작~마감까지의 고가, 저가, 시가, 종가, 거래량을 집계
     * - 종목별 중복 저장 방지
     *
     * @param start 시작 시간 (보통 당일 09:00)
     * @param end   종료 시간 (보통 당일 15:59)
     */
    public void generateDailyOhlcvFrom15min(LocalDateTime start, LocalDateTime end) {
        List<Stock> stocks = stockRepository.findAll();
        LocalDateTime dailyTimestamp = start.toLocalDate().atStartOfDay();

        for (Stock stock : stocks) {
            try {
                List<StockOhlcv> candles = stockOhlcvRepository
                        .findByStockIdAndTimestampBetweenOrderByTimestampAsc(stock.getId(), start, end);

                if (candles.isEmpty()) {
                    log.info("[SKIP] {}: 15분봉 데이터 없음 ({} ~ {})", stock.getSymbol(), start, end);
                    continue;
                }

                double open = candles.get(0).getOpen();
                double close = candles.get(candles.size() - 1).getClose();
                double high = candles.stream().mapToDouble(StockOhlcv::getHigh).max().orElse(open);
                double low = candles.stream().mapToDouble(StockOhlcv::getLow).min().orElse(open);
                long volume = candles.stream().mapToLong(StockOhlcv::getVolume).sum();

                if (volume == 0) {
                    log.warn("[SKIP] {}: 거래량 0 → 저장 생략", stock.getSymbol());
                    continue;
                }

                if (stockOhlcv1dRepository.existsByStockIdAndTimestamp(stock.getId(), dailyTimestamp)) {
                    log.info("[SKIP] {}: 이미 1일봉 존재함 ({})", stock.getSymbol(), dailyTimestamp.toLocalDate());
                    continue;
                }

                StockOhlcv1d ohlcv = StockOhlcv1d.builder()
                        .stock(stock)
                        .timestamp(dailyTimestamp)
                        .open(open)
                        .close(close)
                        .high(high)
                        .low(low)
                        .volume(volume)
                        .build();

                stockOhlcv1dRepository.save(ohlcv);
                log.info("[SAVE] {}: 1일봉 저장 완료 ({})", stock.getSymbol(), dailyTimestamp.toLocalDate());

            } catch (Exception e) {
                log.error("[ERROR] {}: 1일봉 생성 실패", stock.getSymbol(), e);
            }
        }
    }
}