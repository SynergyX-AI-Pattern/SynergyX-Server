package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.databind.JsonNode;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockOhlcv;
import com.synergyx.trading.repository.StockOhlcvRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.service.kisService.client.KisApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.DayOfWeek;


/**
 * KIS API (주식일별분봉조회)를 이용해 하루치 1분봉 전체를 조회 후
 * 15분봉으로 압축해서 stock_ohlcv 테이블에 저장합니다.
 * <p>
 * - 하루치 전체 데이터 수집
 * - 1분봉 → 15분봉 변환 (OHLCV 집계)
 * - 1년치, 다종목 배치 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KisPast15mOhlcvUpdateService {

    private final KisApiClient kisApiClient;
    private final StockOhlcvRepository stockOhlcvRepository;
    private final StockRepository stockRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");

    // 정규장 시간 (09:00 ~ 15:30)
    private static final LocalTime MARKET_OPEN = LocalTime.of(9, 0);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

    /**
     * 단일 종목의 하루치 1분봉 데이터를 모두 가져와 15분봉으로 변환 후 저장합니다.
     *
     * @param stock 종목 엔티티
     * @param date  기준 날짜 (yyyyMMdd)
     */
    @Transactional
    public void updateDaily15mCandles(Stock stock, String date) {
        log.info("[KIS-15m] START - 하루치 수집 시작 stockSymbol={}, date={}", stock.getSymbol(), date);

        try {
            // 하루치 전체 1분봉 데이터 (중복 제거 위해 TreeMap 사용)
            Map<LocalDateTime, JsonNode> oneMinuteCandles = new TreeMap<>();

            // 마지막 봉부터 요청 (15:30 → 09:00까지 내려감)
            LocalTime cursor = MARKET_CLOSE;
            int callCount = 0;

            while (!cursor.isBefore(MARKET_OPEN)) {
                JsonNode candles = request1mCandles(stock.getSymbol(), date, cursor.format(TIME_FMT));
                callCount++;

                if (candles == null || !candles.isArray() || candles.isEmpty()) {
                    log.warn("[KIS-15m] 응답 데이터 없음 - stockSymbol={}, date={}, cursor={}", stock.getSymbol(), date, cursor);
                    break;
                }

                // === 응답 정렬 (과거 → 현재) ===
                List<JsonNode> sorted = new ArrayList<>();
                candles.forEach(sorted::add);
                sorted.sort(Comparator.comparing(n -> n.path("stck_cntg_hour").asText()));

                // === 날짜 필터링 + 중복 제거 ===
                for (JsonNode node : sorted) {
                    try {
                        String nodeDate = node.path("stck_bsop_date").asText();
                        if (!date.equals(nodeDate)) {
//                            log.debug("[KIS-15m] 다른 날짜 데이터 스킵 - stockSymbol={}, targetDate={}, nodeDate={}, time={}",
//                                    stock.getSymbol(), date, nodeDate, node.path("stck_cntg_hour").asText());
                            continue;
                        }

                        LocalDate d = LocalDate.parse(nodeDate, DATE_FMT);
                        LocalTime t = LocalTime.parse(node.path("stck_cntg_hour").asText(), TIME_FMT);
                        LocalDateTime ts = LocalDateTime.of(d, t);

                        oneMinuteCandles.put(ts, node); // 동일 시각이면 최신 값으로 덮어씀
                    } catch (Exception ex) {
                        log.warn("[KIS-15m] 1분봉 파싱 실패 - raw={}", node, ex);
                    }
                }

                // === 다음 cursor 갱신 (가장 과거 봉 기준) ===
                JsonNode oldest = sorted.get(0);
                String oldestTimeStr = oldest.path("stck_cntg_hour").asText();
                LocalTime oldestTime = LocalTime.parse(oldestTimeStr, TIME_FMT);

                if (oldestTime.isBefore(MARKET_OPEN)) {
                    break;
                }
                cursor = oldestTime.minusMinutes(1);

                // API 호출 간격 제한
                Thread.sleep(7000);
            }

            log.info("[KIS-15m] 1분봉 수집 완료 - stockSymbol={}, date={}, 총호출={}, 수집개수={}",
                    stock.getSymbol(), date, callCount, oneMinuteCandles.size());

            if (oneMinuteCandles.isEmpty()) {
                log.warn("[KIS-15m] 데이터 없음 - stockSymbol={}, date={}", stock.getSymbol(), date);
                return;
            }

            // 15분봉으로 집계
            List<StockOhlcv> entities = aggregateTo15mCandles(stock, new ArrayList<>(oneMinuteCandles.values()));

            if (!entities.isEmpty()) {
                int expectedCount = 27;
                int actualCount = entities.size();

                if (actualCount != expectedCount) {
                    log.warn("[KIS-15m] 15분봉 개수 불일치 - stockSymbol={}, date={}, expected={}, actual={}",
                            stock.getSymbol(), date, expectedCount, actualCount);
                } else {
                    log.info("[KIS-15m] 15분봉 개수 정상 - stockSymbol={}, date={}, count={}",
                            stock.getSymbol(), date, actualCount);
                }

                try {
                    stockOhlcvRepository.saveAll(entities);
                    log.info("[KIS-15m] 저장 완료 - {}건 저장됨 stockSymbol={}, date={}", actualCount, stock.getSymbol(), date);
                } catch (DataIntegrityViolationException e) {
                    log.warn("[KIS-15m] 중복 발생 - stockSymbol={}, date={}, 저장 시도 건수={}", stock.getSymbol(), date, actualCount);
                }
            } else {
                log.info("[KIS-15m] 15분봉 없음 - stockSymbol={}, date={}", stock.getSymbol(), date);
            }

        } catch (Exception e) {
            log.error("[KIS-15m] 하루치 수집 실패 - stockSymbol={}, date={}", stock.getSymbol(), date, e);
            throw new RuntimeException("KIS API 호출 실패로 배치 중단 - stockSymbol="
                    + stock.getSymbol() + ", date=" + date, e);
        }
    }

    /**
     * 단일 종목의 과거 1년치 데이터를 하루 단위로 수집합니다.
     */
    @Transactional
    public void updatePastYear(Stock stock) {
        log.info("[KIS-15m] 1년치 수집 시작 - stockSymbol={}", stock.getSymbol());

        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusYears(1);

        for (LocalDate d = oneYearAgo; !d.isAfter(today); d = d.plusDays(1)) {
            // 주말 체크
            if (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
                log.info("[KIS-15m] 휴장일(주말) 스킵 - stockSymbol={}, date={}", stock.getSymbol(), d);
                continue;
            }

            try {
                updateDaily15mCandles(stock, d.format(DATE_FMT));
            } catch (Exception e) {
                log.error("[KIS-15m] 하루 수집 실패 - stockSymbol={}, date={}", stock.getSymbol(), d, e);
                throw e;
            }
        }

        log.info("[KIS-15m] 1년치 수집 완료 - stockSymbol={}", stock.getSymbol());
    }

    /**
     * stock_id 범위를 지정해서 해당 종목들의 1년치 데이터를 수집합니다.
     *
     * @param startId 시작 stock_id (포함)
     * @param endId   끝 stock_id (포함)
     */
    @Transactional
    public void updateStocksByIdRange(long startId, long endId) {
        log.info("[KIS-15m] ID 범위 배치 시작 - {} ~ {}", startId, endId);

        // JPA 메서드로 범위 조회
        List<Stock> stocks = stockRepository.findByIdBetween(startId, endId);

        for (Stock stock : stocks) {
            try {
                updatePastYear(stock);
            } catch (Exception e) {
                log.error("[KIS-15m] 종목 배치 실패 - stockId={}, stockCode={}", stock.getId(), stock.getSymbol(), e);
            }
        }

        log.info("[KIS-15m] ID 범위 배치 완료 - 처리된 종목 수={}", stocks.size());
    }

    @Transactional
    public void testSingleStockById(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 종목을 찾을 수 없습니다. stockId=" + stockId));

        testSingleStock(stock);
    }

    /**
     * 단일 종목 테스트 실행 (오늘 하루치만 수집)
     */
    public void testSingleStock(Stock stock) {
        String today = LocalDate.now().format(DATE_FMT);
        log.info("[KIS-15m] 테스트 실행 - stockSymbol={}, date={}", stock.getSymbol(), today);
        updateDaily15mCandles(stock, today);
    }

    // ================== 내부 유틸 메서드 ================== //

    /**
     * KIS API 1분봉 호출
     */
    private JsonNode request1mCandles(String symbol, String baseDate, String baseHour) {
        JsonNode response = null;
        try {
            response = kisApiClient.get(
                    "/uapi/domestic-stock/v1/quotations/inquire-time-dailychartprice",
                    Map.of(
                            "fid_cond_mrkt_div_code", "J",
                            "fid_input_iscd", symbol,
                            "fid_input_hour_1", baseHour,
                            "fid_input_date_1", baseDate,
                            "FID_PW_DATA_INCU_YN", "Y",   // 과거 데이터 포함
                            "FID_FAKE_TICK_INCU_YN", " "  // 허봉 제외 (공백 필수)
                    ),
                    "FHKST03010230"
            );

            if (response == null) {
                throw new RuntimeException("[KIS] 응답이 null - stockSymbol=" + symbol + ", date=" + baseDate + ", baseHour=" + baseHour);
            }

            // === Raw 응답 로그 ===
//            log.debug("[KIS-RAW] stockSymbol={}, date={}, baseHour={} 응답={}",
//                    symbol, baseDate, baseHour, response.toPrettyString());

            return response.path("output2");

        } catch (Exception e) {
            // 실패 시 수집 중단
            log.error("[KIS-RAW] API 호출 실패 - stockSymbol={}, date={}, baseHour={}", symbol, baseDate, baseHour, e);
            throw new RuntimeException("[KIS API 호출 실패] stockSymbol=" + symbol + ", date=" + baseDate + ", baseHour=" + baseHour, e);
        }
    }

    /**
     * 1분봉 데이터를 15분 단위 그룹으로 압축합니다.
     * <p>
     * - open: 구간 첫 시가
     * - close: 구간 마지막 종가
     * - high: 구간 내 고가 중 최댓값
     * - low: 구간 내 저가 중 최솟값
     * - volume: 구간 내 거래량 합계
     * - 모든 15분 구간 생성 (09:00 ~ 15:30 총 27개)
     * - 거래량 없으면 0, 가격은 직전 종가로 유지
     * - 15:30 독립 봉 생성 (실시간 수집과 동일)
     */
    private List<StockOhlcv> aggregateTo15mCandles(Stock stock, List<JsonNode> oneMinuteCandles) {
        List<StockOhlcv> results = new ArrayList<>();

        if (oneMinuteCandles == null || oneMinuteCandles.isEmpty()) {
            log.warn("[KIS-15m] 입력된 1분봉 데이터 없음 - stockSymbol={}", stock.getSymbol());
            return results;
        }

        try {
            // ===== 1. 중복 제거 (timestamp 기준) =====
            Map<LocalDateTime, JsonNode> uniqueCandles = new TreeMap<>();
            for (JsonNode node : oneMinuteCandles) {
                try {
                    LocalDate d = LocalDate.parse(node.path("stck_bsop_date").asText(), DATE_FMT);
                    LocalTime t = LocalTime.parse(node.path("stck_cntg_hour").asText(), TIME_FMT);
                    LocalDateTime ts = LocalDateTime.of(d, t);

                    uniqueCandles.put(ts, node);
                } catch (Exception ex) {
                    log.warn("[KIS-15m] 1분봉 파싱 실패 - raw={}", node, ex);
                }
            }

            List<Map.Entry<LocalDateTime, JsonNode>> candleList = new ArrayList<>(uniqueCandles.entrySet());
            candleList.sort(Map.Entry.comparingByKey());

            LocalDate tradingDate = candleList.get(0).getKey().toLocalDate();

            // ===== 2. 1분봉 → 15분 버킷 그룹화 =====
            Map<LocalDateTime, List<JsonNode>> grouped = new TreeMap<>();
            for (Map.Entry<LocalDateTime, JsonNode> entry : candleList) {
                LocalDateTime ts = entry.getKey();
                JsonNode node = entry.getValue();

                int bucket = ts.getMinute() / 15;
                LocalTime bucketStartTime = LocalTime.of(ts.getHour(), bucket * 15);

                if (ts.toLocalTime().equals(MARKET_CLOSE)) {
                    bucketStartTime = MARKET_CLOSE;
                }

                LocalDateTime bucketKey = LocalDateTime.of(tradingDate, bucketStartTime);
                grouped.computeIfAbsent(bucketKey, k -> new ArrayList<>()).add(node);
            }

            // ===== 3. 모든 구간(09:00 ~ 15:30) 강제 생성 =====
            List<LocalDateTime> intervals = new ArrayList<>();
            for (LocalTime t = MARKET_OPEN; !t.isAfter(MARKET_CLOSE); t = t.plusMinutes(15)) {
                intervals.add(LocalDateTime.of(tradingDate, t));
            }

            Double lastClose = null;

            for (LocalDateTime bucketStart : intervals) {
                List<JsonNode> group = grouped.getOrDefault(bucketStart, Collections.emptyList());
                StockOhlcv candle;

                if (!group.isEmpty()) {
                    JsonNode oldest = group.get(0);                       // 가장 과거
                    JsonNode newest = group.get(group.size() - 1);        // 가장 최근

                    double open = oldest.path("stck_oprc").asDouble();
                    double close = newest.path("stck_prpr").asDouble();
                    double high = group.stream().mapToDouble(n -> n.path("stck_hgpr").asDouble()).max().orElse(open);
                    double low = group.stream().mapToDouble(n -> n.path("stck_lwpr").asDouble()).min().orElse(open);
                    long volume = group.stream().mapToLong(n -> n.path("cntg_vol").asLong()).sum();

                    candle = StockOhlcv.builder()
                            .stock(stock)
                            .timestamp(bucketStart)
                            .open(open)
                            .high(high)
                            .low(low)
                            .close(close)
                            .volume(volume)
                            .build();

                    lastClose = close;
                } else if (lastClose != null) {
                    candle = StockOhlcv.builder()
                            .stock(stock)
                            .timestamp(bucketStart)
                            .open(lastClose)
                            .high(lastClose)
                            .low(lastClose)
                            .close(lastClose)
                            .volume(0L)
                            .build();
                } else {
                    log.warn("[KIS-15m] {} 구간 데이터 없음, 직전 종가도 없어 skip - stockSymbol={}", bucketStart, stock.getSymbol());
                    continue;
                }

                results.add(candle);

//                log.debug("[KIS-15m] {} 압축 봉 생성 O={} H={} L={} C={} V={}",
//                        bucketStart, candle.getOpen(), candle.getHigh(), candle.getLow(), candle.getClose(), candle.getVolume());
            }

        } catch (Exception e) {
            log.error("[KIS-15m] 15분봉 집계 중 예외 발생 - stockSymbol={}", stock.getSymbol(), e);
        }

        return results;
    }

}
