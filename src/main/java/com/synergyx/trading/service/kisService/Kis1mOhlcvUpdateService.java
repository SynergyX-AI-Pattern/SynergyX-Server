package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.databind.JsonNode;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockOhlcv1m;
import com.synergyx.trading.repository.StockOhlcv1mRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.service.kisService.client.KisApiClient;
import com.synergyx.trading.util.OhlcvCompareUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.synergyx.trading.util.ParsingUtil.toDouble;
import static com.synergyx.trading.util.ParsingUtil.toLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class Kis1mOhlcvUpdateService {

    private final KisApiClient kisApiClient;
    private final StockRepository stockRepository;
    private final StockOhlcv1mRepository stockOhlcv1mRepository;

    private static final int REQUEST_INTERVAL_MILLIS = 5000; // 5초
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    /**
     * 전체 종목의 과거 5년 월봉 데이터를 업데이트합니다.
     */
    public void updateOhlcvAll() {
        List<Stock> stocks = stockRepository.findAll();

        for (int i = 0; i < stocks.size(); i++) {
            Stock stock = stocks.get(i);
            log.info("[{} / {}] {} 종목 월봉 수집 시작", i + 1, stocks.size(), stock.getSymbol());

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

        log.info("전체 종목 월봉 수집 완료");
    }

    /**
     * 특정 stockId(또는 범위)의 종목들에 대해 월봉 데이터를 업데이트합니다.
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
                Thread.sleep(REQUEST_INTERVAL_MILLIS);
            } catch (Exception e) {
                log.error("[OHLCV] {} (id={}) 업데이트 실패: {}", stock.getSymbol(), stock.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 종목의 월봉 데이터를 갱신합니다.
     *
     * @param stock 종목
     */
    @Transactional
    public void updateOhlcvInternal(Stock stock) {
        log.info("[START] {} 종목의 월봉 수집 시작", stock.getSymbol());

        LocalDateTime end = LocalDateTime.now().withDayOfMonth(1); // 이번 달 1일
        LocalDateTime minTimestamp = end.minusYears(5); // 5년 이전

        while (true) {

            if (end.isBefore(minTimestamp)) {
                log.info("[END] {}: 수집 범위 종료 (end < minTimestamp)", stock.getSymbol());
                break;
            }

            String endDate = end.format(DATE_FORMAT);
            String startDate = minTimestamp.format(DATE_FORMAT); // 최대 100건 요청

            // 날짜 역전 방지
            if (LocalDate.parse(startDate, DATE_FORMAT).isAfter(LocalDate.parse(endDate, DATE_FORMAT))) {
                log.warn("[SKIP] 잘못된 날짜 범위: startDate={} > endDate={}", startDate, endDate);
                break;
            }

            JsonNode candles = fetchOhlcvCandles(stock.getSymbol(), startDate, endDate);
            if (!candles.isArray() || candles.isEmpty()) {
                log.warn("[SKIP] {}: 월봉 데이터 없음 또는 수집 실패 ({} ~ {})", stock.getSymbol(), startDate, endDate);
                break;
            }

            log.info("[FETCH] {}: {}개 월봉 수집 완료 ({} ~ {})",
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

                saveMonthlyCandle(candle, stock);
            }

            if (reachedMinimum) {
                log.info("[END] {}: 수집 완료 (기준 도달: {})", stock.getSymbol(), minTimestamp.toLocalDate());
                return;
            }

            // 다음 요청 범위 조정
            String earliest = sorted.get(0).path("stck_bsop_date").asText();
            end = LocalDateTime.parse(earliest + "000000", DATE_TIME_FORMAT).minusMonths(1);

            sleep(REQUEST_INTERVAL_MILLIS);
        }

        log.info("[DONE] {} 종목의 월봉 수집 완료", stock.getSymbol());
    }

    /**
     * 월봉 데이터를 저장합니다.
     *
     * @param candle
     * @param stock
     */
    private void saveMonthlyCandle(JsonNode candle, Stock stock) {
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
            log.warn("[SKIP] 유효하지 않은 월봉 캔들: {} {}", stock.getSymbol(), dateStr);
            return;
        }

        Optional<StockOhlcv1m> existing = stockOhlcv1mRepository.findByStock_IdAndTimestamp(stock.getId(), timestamp);
        if (existing.isPresent()) {
            StockOhlcv1m current = existing.get();
            if (OhlcvCompareUtil.isDifferent(current, open, high, low, close, volume)) {
                current.setOpen(open);
                current.setHigh(high);
                current.setLow(low);
                current.setClose(close);
                current.setVolume(volume);
                stockOhlcv1mRepository.save(current);
                log.info("[UPDATE] 월봉 수정: {} {}", stock.getSymbol(), timestamp.toLocalDate());
            } else {
                log.debug("[SKIP] 동일한 월봉 존재: {} {}", stock.getSymbol(), timestamp.toLocalDate());
            }
        } else {
            StockOhlcv1m newCandle = StockOhlcv1m.builder()
                    .stock(stock)
                    .timestamp(timestamp)
                    .open(open)
                    .high(high)
                    .low(low)
                    .close(close)
                    .volume(volume)
                    .build();

            stockOhlcv1mRepository.save(newCandle);
            log.info("[INSERT] 월봉 저장: {} {}", stock.getSymbol(), timestamp.toLocalDate());
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
        log.debug("[REQ] OHLCV 월봉 요청: symbol={}, startDate={}, endDate={}", symbol, startDate, endDate);
        JsonNode response = kisApiClient.get(
                "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice",
                Map.of(
                        "fid_cond_mrkt_div_code", "J",
                        "fid_input_iscd", symbol,
                        "fid_input_date_1", startDate,
                        "fid_input_date_2", endDate,
                        "FID_PERIOD_DIV_CODE", "M", // D:일봉 W:주봉, M:월봉, Y:년봉
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