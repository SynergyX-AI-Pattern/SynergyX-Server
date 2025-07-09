package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.databind.JsonNode;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockOhlcv1d;
import com.synergyx.trading.repository.StockOhlcv1dRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.service.kisService.client.KisApiClient;
import com.synergyx.trading.util.OhlcvCompareUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
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
public class Kis1dOhlcvUpdateService {

    private final KisApiClient kisApiClient;
    private final StockRepository stockRepository;
    private final StockOhlcv1dRepository stockOhlcv1dRepository;

    private static final int REQUEST_INTERVAL_MILLIS = 10_000;
    private static final int BETWEEN_STOCK_SLEEP_MILLIS = 20_000;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    /**
     * 전체 종목의 과거 3개월 1일봉 데이터를 업데이트합니다.
     */
    public void updateOhlcvAll() {
        List<Stock> stocks = stockRepository.findAll();

        for (int i = 0; i < stocks.size(); i++) {
            Stock stock = stocks.get(i);
            log.info("[{} / {}] {} 종목 1일봉 수집 시작", i + 1, stocks.size(), stock.getSymbol());

            try {
                updateOhlcvInternal(stock);
            } catch (Exception e) {
                log.error("[OHLCV] {} 수집 실패: {}", stock.getSymbol(), e.getMessage(), e);
            }

            try {
                log.info("{} 종목 완료. 다음 종목까지 10초 대기", stock.getSymbol());
                Thread.sleep(REQUEST_INTERVAL_MILLIS); // 10초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("전체 종목 1일봉 수집 완료");
    }

    /**
     * 특정 stockId(또는 범위)의 종목들에 대해 1일봉 데이터를 업데이트합니다.
     *
     * @param startId 시작 stockId (필수)
     * @param endId   종료 stockId (옵션)
     */
    @Transactional
    public void updateOhlcvByStockIdRange(Long startId, @Nullable Long endId) {
        List<Stock> stocks;

        if (endId == null || startId.equals(endId)) {
            // 단일 종목
            Stock stock = stockRepository.findById(startId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 stockId 없음: " + startId));
            stocks = List.of(stock);
        } else {
            // 범위 조회
            stocks = stockRepository.findByIdBetween(startId, endId);
            if (stocks.isEmpty()) {
                log.warn("해당 범위에 종목이 없습니다: {} ~ {}", startId, endId);
                return;
            }
        }

        for (Stock stock : stocks) {
            try {
                updateOhlcvInternal(stock);
                Thread.sleep(BETWEEN_STOCK_SLEEP_MILLIS);
            } catch (Exception e) {
                log.error("[OHLCV] {} (id={}) 업데이트 실패: {}", stock.getSymbol(), stock.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 종목의 1일봉 데이터를 갱신합니다.
     *
     * @param stock 종목
     */
    @Transactional
    public void updateOhlcvInternal(Stock stock) {
        log.info("[START] {} 종목의 1일봉 수집 시작", stock.getSymbol());

        LocalDateTime end = LocalDateTime.now().minusDays(1); // 오늘 제외
//        LocalDateTime minTimestamp = end.minusMonths(3); // 3개월 이전
        LocalDateTime minTimestamp = end.minusDays(99); //  99일

        while (true) {
            String endDate = end.format(DATE_FORMAT);
            String startDate = end.minusDays(99).format(DATE_FORMAT); // 최대 100건 요청

            JsonNode candles = fetchOhlcvCandles(stock.getSymbol(), startDate, endDate);
            if (!candles.isArray() || candles.isEmpty()) {
                log.warn("[SKIP] {}: 일봉 데이터 없음 또는 수집 실패 ({} ~ {})", stock.getSymbol(), startDate, endDate);
                break;
            }

            log.info("[FETCH] {}: {}개 일봉 수집 완료 ({} ~ {})",
                    stock.getSymbol(), candles.size(), startDate, endDate);

            List<JsonNode> sorted = new ArrayList<>();
            candles.forEach(sorted::add);
            sorted.sort(Comparator.comparing(n -> n.path("stck_bsop_date").asText()));

            boolean reachedMinimum = false;

            for (JsonNode candle : sorted) {
                String dateStr = candle.path("stck_bsop_date").asText();
                LocalDateTime candleDate = LocalDateTime.parse(dateStr + "000000", DATE_TIME_FORMAT);

                if (candleDate.isBefore(minTimestamp)) {
                    reachedMinimum = true;
                }

                if (isWeekend(candleDate)) {
                    log.debug("[SKIP] {}: 주말 캔들 제외 ({})", stock.getSymbol(), candleDate.toLocalDate());
                    continue;
                }

                saveDailyCandle(candle, stock);
            }

            if (reachedMinimum) {
                log.info("[END] {}: 수집 완료 (기준 도달: {})", stock.getSymbol(), minTimestamp.toLocalDate());
                return;
            }

            // 다음 요청 범위 조정
            String earliest = sorted.get(0).path("stck_bsop_date").asText();
            end = LocalDateTime.parse(earliest + "000000", DATE_TIME_FORMAT).minusDays(1);

            sleep(REQUEST_INTERVAL_MILLIS);
        }

        log.info("[DONE] {} 종목의 1일봉 수집 완료", stock.getSymbol());
    }

    /**
     * 주말 여부를 판별합니다.
     */
    private boolean isWeekend(LocalDateTime date) {
        int day = date.getDayOfWeek().getValue();
        return day == 6 || day == 7;
    }

    /**
     * 일봉 데이터를 저장합니다.
     *
     * @param candle
     * @param stock
     */
    private void saveDailyCandle(JsonNode candle, Stock stock) {
        String dateStr = candle.path("stck_bsop_date").asText();
        if (dateStr.isBlank()) {
            log.warn("[SKIP] 날짜 누락된 캔들: {}", candle.toPrettyString());
            return;
        }

        LocalDateTime timestamp = LocalDateTime.parse(dateStr + "000000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Double open = toDouble(candle.path("stck_oprc").asText());
        Double high = toDouble(candle.path("stck_hgpr").asText());
        Double low = toDouble(candle.path("stck_lwpr").asText());
        Double close = toDouble(candle.path("stck_clpr").asText());
        Long volume = toLong(candle.path("acml_vol").asText());

        if (open == null || close == null || high == null || low == null || volume == null || volume == 0) {
            log.warn("[SKIP] 유효하지 않은 일봉 캔들: {} {}", stock.getSymbol(), dateStr);
            return;
        }

        Optional<StockOhlcv1d> existing = stockOhlcv1dRepository.findByStock_IdAndTimestamp(stock.getId(), timestamp);
        if (existing.isPresent()) {
            StockOhlcv1d current = existing.get();
            if (OhlcvCompareUtil.isDifferent(current, open, high, low, close, volume)) {
                current.setOpen(open);
                current.setHigh(high);
                current.setLow(low);
                current.setClose(close);
                current.setVolume(volume);
                stockOhlcv1dRepository.save(current);
                log.info("[UPDATE] 일봉 수정: {} {}", stock.getSymbol(), timestamp.toLocalDate());
            } else {
                log.debug("[SKIP] 동일한 일봉 존재: {} {}", stock.getSymbol(), timestamp.toLocalDate());
            }
        } else {
            StockOhlcv1d newCandle = StockOhlcv1d.builder()
                    .stock(stock)
                    .timestamp(timestamp)
                    .open(open)
                    .high(high)
                    .low(low)
                    .close(close)
                    .volume(volume)
                    .build();

            stockOhlcv1dRepository.save(newCandle);
            log.info("[INSERT] 일봉 저장: {} {}", stock.getSymbol(), timestamp.toLocalDate());
        }
    }

    /**
     * 특정 종목의 주식일별분봉조회 데이터를 요청합니다.
     * KIS inquire-daily-itemchartprice API (TR_ID: FHKST03010100) 호출.
     *
     * @param symbol    종목 코드
     * @param startDate
     * @param endDate
     * @return 캔들 JSON 배열
     */
    private JsonNode fetchOhlcvCandles(String symbol, String startDate, String endDate) {
        log.debug("[REQ] OHLCV 일봉 요청: symbol={}, startDate={}, endDate={}", symbol, startDate, endDate);
        JsonNode response = kisApiClient.get(
                "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice",
                Map.of(
                        "fid_cond_mrkt_div_code", "J",
                        "fid_input_iscd", symbol,
                        "fid_input_date_1", startDate,
                        "fid_input_date_2", endDate,
                        "FID_PERIOD_DIV_CODE", "D", // D:일봉 W:주봉, M:월봉, Y:년봉
                        "FID_ORG_ADJ_PRC", "0"
                ),
                "FHKST03010100"
        );
//        log.debug("[RAW response] {}", response.toPrettyString());
        return response.path("output2");
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}