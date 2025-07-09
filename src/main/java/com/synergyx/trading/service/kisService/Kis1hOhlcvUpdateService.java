package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.databind.JsonNode;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockOhlcv1h;
import com.synergyx.trading.repository.StockOhlcv1hRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.service.kisService.client.KisApiClient;
import com.synergyx.trading.util.OhlcvCompareUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.synergyx.trading.util.ParsingUtil.toDouble;
import static com.synergyx.trading.util.ParsingUtil.toLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class Kis1hOhlcvUpdateService {

    private final KisApiClient kisApiClient;
    private final StockRepository stockRepository;
    private final StockOhlcv1hRepository stockOhlcv1hRepository;

    private static final int REQUEST_INTERVAL_MILLIS = 10_000;
    private static final int BETWEEN_STOCK_SLEEP_MILLIS = 20_000;

    /**
     * 전체 종목의 과거 3개월 1시간봉 데이터를 업데이트합니다.
     */
    public void updateOhlcvAll() {
        List<Stock> stocks = stockRepository.findAll();

        for (int i = 0; i < stocks.size(); i++) {
            Stock stock = stocks.get(i);
            log.info("▶ [{} / {}] {} 종목 1시간봉 수집 시작", i + 1, stocks.size(), stock.getSymbol());

            try {
                updateOhlcvInternal(stock);
            } catch (Exception e) {
                log.error("[OHLCV] {} 수집 실패: {}", stock.getSymbol(), e.getMessage(), e);
            }

            try {
                log.info("⏸ {} 종목 완료. 다음 종목까지 10초 대기", stock.getSymbol());
                Thread.sleep(REQUEST_INTERVAL_MILLIS); // 10초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.info("전체 종목 1시간봉 수집 완료");
    }

    /**
     * 특정 stockId(또는 범위)의 종목들에 대해 1시간봉 데이터를 업데이트합니다.
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
     * 종목의 1시간봉 데이터를 갱신합니다.
     *
     * @param stock 종목
     */
    @Transactional
    public void updateOhlcvInternal(Stock stock) {
        log.info("[{}] 3개월 1시간봉 수집 시작", stock.getSymbol());

        // 15:30부터 시작
        LocalDateTime end = LocalDateTime.now().withHour(15).withMinute(59).withSecond(59).withNano(0);
//        LocalDateTime end = LocalDateTime.of(2025, 6, 29, 15, 59, 59); 테스트용

        // 3개월 전
        LocalDateTime start = end.minusMonths(3);

        // 요청 기준 시간 리스트 (15:59, 14:59, 13:59, 12:59, 11:59, 10:59, 09:59)
        while (!end.isBefore(start)) {

            // 주말 제외
            if (end.getDayOfWeek().getValue() >= 6) { // 6 = 토요일, 7 = 일요일
                log.info("[SKIP] 주말 수집 생략: {}", end.toLocalDate());
                end = end.minusHours(1);
                continue;
            }

            if (end.isAfter(LocalDateTime.of(end.toLocalDate(), LocalTime.of(15, 59, 59)))) {
                log.debug("[SKIP] 장 마감 이후 시간 생략: {}", end);
                end = end.withHour(15).withMinute(30).withSecond(0).withNano(0);
                continue;
            }

            // 9시 이전 요청 제한
            if (end.isBefore(LocalDateTime.of(end.toLocalDate(), LocalTime.of(9, 0)))) {
                log.info("[END] 08:59 이전이므로 요청 중단: {}", end);
                end = end.minusDays(1).withHour(15).withMinute(59).withSecond(0).withNano(0);
                continue;
            }

            String date = end.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String hour = end.format(DateTimeFormatter.ofPattern("HHmmss"));

            JsonNode candles = fetchOhlcvCandles(stock.getSymbol(), date, hour);
            if (!candles.isArray() || candles.isEmpty()) {
                log.warn("[SKIP] {} 수집 실패 or 응답 없음 (date={}, hour={})", stock.getSymbol(), date, hour);
                end = end.minusHours(1);
                continue;
            }

            log.info("[FETCH] {} 캔들 수집 성공 ({}개)", stock.getSymbol(), candles.size());

            // 저장
            LocalDateTime next = parseAndSaveGroupedCandles(candles, stock, end);
            end = next;

            sleep(REQUEST_INTERVAL_MILLIS); // 10초 대기
        }

        log.info(" [{}] 3개월 1시간봉 수집 완료", stock.getSymbol());
    }

    /**
     * 캔들 데이터를 파싱하고 그룹핑하여 저장합니다. (1시간 /30분 봉)
     *
     * @param candles     캔들 배열
     * @param stock       종목
     * @param requestTime 요청 시간 (1시간 단위로 요청)
     * @return 다음 호출 기준시간 (요청 시각 1시간 이전 시각)
     */
    private LocalDateTime parseAndSaveGroupedCandles(JsonNode candles, Stock stock, LocalDateTime requestTime) {
        Map<LocalDateTime, List<JsonNode>> grouped = new TreeMap<>();

        for (JsonNode candle : candles) {
            String date = candle.path("stck_bsop_date").asText();
            String hour = candle.path("stck_cntg_hour").asText();
            LocalDateTime ts = LocalDateTime.parse(date + hour, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            LocalDateTime groupKey = toGroupKey(ts);

            grouped.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(candle);
        }

        log.debug("[GROUP] 캔들 그룹 개수 ({}개)", grouped.size());

        // 요청 기준 시간에 해당하는 그룹만 저장
        LocalDateTime saveKey = toGroupKey(requestTime);

        if (!grouped.containsKey(saveKey)) {
            log.warn("[MISMATCH] 저장 대상 그룹키({})가 응답에 없음. 총 {}개 그룹 수신: {}",
                    saveKey.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    grouped.size(),
                    grouped.keySet().stream()
                            .map(k -> k.format(DateTimeFormatter.ofPattern("HH:mm")))
                            .collect(Collectors.joining(", ")));
        } else {
            log.debug("[MATCH] 저장 대상 그룹키({}) 존재. (캔들 수: {})",
                    saveKey.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    grouped.get(saveKey).size());
        }

        List<JsonNode> group = grouped.getOrDefault(saveKey, Collections.emptyList());
        saveCandleGroup(saveKey, group, stock);

        return requestTime.minusHours(1);
    }

    /**
     * 캔들 데이터를 DB에 저장합니다.
     *
     * @param timestamp
     * @param group
     * @param stock
     */
    private void saveCandleGroup(LocalDateTime timestamp, List<JsonNode> group, Stock stock) {

        // 체결이 없는 경우 건너뜀
        if (group.isEmpty()) {
            log.info("[SKIP] 체결 없음: {} {} → 저장 생략", stock.getSymbol(), timestamp);
            return;
        }

        group.sort(Comparator.comparing(n -> LocalDateTime.parse(
                n.path("stck_bsop_date").asText() + n.path("stck_cntg_hour").asText(),
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        )));

        Double open = toDouble(group.get(0).path("stck_oprc").asText());
        Double close = toDouble(group.get(group.size() - 1).path("stck_prpr").asText());
        Double high = group.stream().map(n -> toDouble(n.path("stck_hgpr").asText())).max(Double::compare).orElse(null);
        Double low = group.stream().map(n -> toDouble(n.path("stck_lwpr").asText())).min(Double::compare).orElse(null);
        Long volume = group.stream().map(n -> toLong(n.path("cntg_vol").asText())).reduce(0L, Long::sum);

        if (open == null || close == null || high == null || low == null || volume == null || volume == 0) {
            log.warn("[SKIP] 유효하지 않은 캔들 그룹: {} {}", stock.getSymbol(), timestamp);
            return;
        }

        log.debug("[GROUPED] {} → {}분봉 생성 (캔들 수: {})",
                timestamp,
                (timestamp.getHour() == 15 && timestamp.getMinute() == 0) ? 30 : 60,
                group.size());

        Optional<StockOhlcv1h> existing = stockOhlcv1hRepository.findByStock_IdAndTimestamp(stock.getId(), timestamp);
        if (existing.isPresent()) {
            StockOhlcv1h current = existing.get();
            if (OhlcvCompareUtil.isDifferent(current, open, high, low, close, volume)) {
                current.setOpen(open);
                current.setHigh(high);
                current.setLow(low);
                current.setClose(close);
                current.setVolume(volume);
                stockOhlcv1hRepository.save(current);
                log.info("[UPDATE] {} {}", stock.getSymbol(), timestamp);
            } else {
                log.debug("[SKIP] 동일 캔들 존재: {} {}", stock.getSymbol(), timestamp);
            }
        } else {
            StockOhlcv1h newCandle = StockOhlcv1h.builder()
                    .stock(stock)
                    .timestamp(timestamp)
                    .open(open)
                    .high(high)
                    .low(low)
                    .close(close)
                    .volume(volume)
                    .build();

            stockOhlcv1hRepository.save(newCandle);
            log.info("[INSERT] 신규 저장: {} {}", stock.getSymbol(), timestamp);
        }
    }

    /**
     * 특정 종목의 주식일별분봉조회 데이터를 요청합니다.
     * KIS inquire-time-dailychartprice API (TR_ID: FHKST03010230) 호출.
     *
     * @param symbol 종목 코드
     * @param date   기준 날짜 (yyyyMMdd)
     * @param hour   기준 시간 (HHmmss)
     * @return 캔들 JSON 배열
     */
    private JsonNode fetchOhlcvCandles(String symbol, String date, String hour) {
        JsonNode response = kisApiClient.get(
                "/uapi/domestic-stock/v1/quotations/inquire-time-dailychartprice",
                Map.of(
                        "fid_cond_mrkt_div_code", "J",
                        "fid_input_iscd", symbol,
                        "fid_input_hour_1", hour,
                        "fid_input_date_1", date,
                        "FID_PW_DATA_INCU_YN", "N", // 정규장 데이터만 조회
                        "FID_FAKE_TICK_INCU_YN", ""
                ),
                "FHKST03010230"
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

    private LocalDateTime toGroupKey(LocalDateTime ts) {
        return (ts.getHour() == 15 && ts.getMinute() > 0)
                ? LocalDateTime.of(ts.toLocalDate(), LocalTime.of(15, 0))
                : ts.withMinute(0).withSecond(0).withNano(0);
    }
}