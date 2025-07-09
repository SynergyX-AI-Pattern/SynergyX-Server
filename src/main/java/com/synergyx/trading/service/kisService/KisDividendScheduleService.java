package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockDetail;
import com.synergyx.trading.repository.StockDetailRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.service.kisService.client.KisApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.synergyx.trading.util.ParsingUtil.toDouble;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisDividendScheduleService {

    private final KisApiClient kisApiClient;
    private final StockRepository stockRepository;
    private final StockDetailRepository stockDetailRepository;
    private final ObjectMapper objectMapper;

    private static final int REQUEST_INTERVAL_MILLIS = 400;

    /**
     * 전체 종목의 배당금 (dividend_amount)을 업데이트합니다.
     */
    @Transactional
    public void updateDividendAll() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            try {
                updateDividendAmountInternal(stock);
                Thread.sleep(REQUEST_INTERVAL_MILLIS); // API 호출 제한
            } catch (Exception e) {
                log.error("[DIV] {} 배당금 업데이트 실패: {}", stock.getSymbol(), e.getMessage(), e);
            }
        }
    }

    /**
     * 개별 종목의 배당금 (dividend_amount)을 업데이트합니다.
     */
    @Transactional
    public void updateDividendBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("해당 종목 없음: " + symbol));

        try {
            updateDividendAmountInternal(stock);
        } catch (Exception e) {
            log.error("[DIV] {} 배당금 업데이트 실패: {}", symbol, e.getMessage(), e);
        }
    }

    /**
     * 개별 종목의 배당금을 저장합니다.
     *
     * @param stock
     * @throws Exception
     */
    public void updateDividendAmountInternal(Stock stock) throws Exception {
        JsonNode output = fetchAndParseDividendResponse(stock);
        if (output == null) {
            log.warn("[DIV] 응답에 배당 정보 없음 - symbol: {}", stock.getSymbol());
            return;
        }

        Double totalDividend = calculateValidAnnualDividend(output, stock.getSymbol());
        if (totalDividend == null || totalDividend <= 0) {
            log.warn("[DIV] 유효 배당금 없음 - symbol: {}", stock.getSymbol());
            return;
        }

        updateStockDetailWithDividend(stock, totalDividend);
    }

    /**
     * 연간 배당일정 데이터를 파싱합니다.
     *
     * @param stock
     * @return
     * @throws IOException
     */
    private JsonNode fetchAndParseDividendResponse(Stock stock) throws IOException {
        ObjectNode response = fetchDividendSchedule(stock.getSymbol());

//        log.info("[DIV] 응답 전체 JSON ({}):\n{}", stock.getSymbol(),
//                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

        JsonNode output = response.get("output1");
        return (output != null && output.isArray()) ? output : null;
    }

    /**
     * 연간 유효 배당금 합산 계산
     * - 지급일 존재 + 배당금 > 0 + (종류: 없음 or "보통")인 항목만 유효로 간주
     * - 기준일(record_date) 기준으로 연도 그룹핑
     * - 기준 연도: 최근 1년(없으면 2년 fallback)
     * - 결산배당이면서 다음해 4월 이내 지급 → 전년도(record_date) 기준 연도에 포함
     *
     * @param output KIS 배당 정보 응답 (output1)
     * @param symbol 종목 코드
     * @return 연간 배당금 (없으면 null)
     */
    private Double calculateValidAnnualDividend(JsonNode output, String symbol) {
        // 연도별 배당금 합산
        Map<String, Double> yearToSum = buildAnnualDividendMap(output, symbol);
//        log.info("[DIV] yearToSum = {}", yearToSum);

        String lastYear = String.valueOf(LocalDate.now().getYear() - 1);
        String prevYear = String.valueOf(LocalDate.now().getYear() - 2);

        // 2024년 배당금 우선 사용, 없으면 2023으로 fallback
        Double totalDividend = yearToSum.getOrDefault(lastYear, 0.0);
        if (totalDividend > 0) return totalDividend;

        totalDividend = yearToSum.getOrDefault(prevYear, 0.0);
        if (totalDividend > 0) {
//            log.info("[DIV] {}년도 배당금 fallback 사용 - symbol: {}, totalDividend: {}", prevYear, symbol, totalDividend);
            return totalDividend;
        }

        return null;
    }

    /**
     * 유효한 배당 정보를 기준일(record_date) 기준으로 연도별 그룹핑하여 합산
     */
    private Map<String, Double> buildAnnualDividendMap(JsonNode output, String symbol) {
        return StreamSupport.stream(output.spliterator(), false)
                .filter(node -> symbol.equals(node.path("sht_cd").asText("")))
                .map(this::mapToDividendWithRecordYear)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        DividendEntry::year,
                        Collectors.summingDouble(DividendEntry::amount)
                ));
    }

    /**
     * 배당 정보를 유효성 검증 후 기준연도 + 금액으로 매핑
     * - 지급일 존재 + 오늘 기준 이미 지급된 건
     * - 보통주
     * - 기준연도: record_date의 연도
     * - 결산 배당이면서 다음해 4월 이내 지급된 건도 그대로 record_date 기준 연도에 포함
     */
    private DividendEntry mapToDividendWithRecordYear(JsonNode node) {
        String payDateStr = node.path("divi_pay_dt").asText("");
        String amountStr = node.path("per_sto_divi_amt").asText("");
        String kind = node.path("stk_kind").asText("");
        String diviKind = node.path("divi_kind").asText("");

        String recordDate = node.path("record_date").asText("");

        if (recordDate.length() < 4 || payDateStr.isBlank() || amountStr.isBlank() || amountStr.equals("0"))
            return null;
        if (!kind.isBlank() && !kind.equals("보통")) return null;

        try {
            LocalDate payDate = LocalDate.parse(payDateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            if (payDate.isAfter(LocalDate.now())) return null; // 아직 지급되지 않음

            double amount = toDouble(amountStr);

            String year;
            if ("결산".equals(diviKind)) {
                if (payDate.getMonthValue() <= 4) {
                    year = String.valueOf(payDate.getYear() - 1); // 4월 이전인 경우 전년도로 처리
                } else {
                    year = String.valueOf(payDate.getYear()); // fallback
                }
            } else {
                year = String.valueOf(payDate.getYear());
            }

//            log.info("[DIV] 유효 배당 - symbol: {}, year: {}, amount: {}", node.path("sht_cd").asText(), year, amount);

            return new DividendEntry(year, amount);

        } catch (Exception e) {
            log.warn("[DIV] 배당 파싱 실패 - symbol: {}, record_date: {}, 지급일: {}", node.path("sht_cd").asText(""), recordDate, payDateStr);
            return null;
        }
    }

    /**
     * 연도별 배당 합산을 위한 내부 구조
     */
    private record DividendEntry(String year, double amount) {
    }

    /**
     * 배당금을 기존 값과 비교 후 db에 저장합니다.
     *
     * @param stock
     * @param totalDividend
     * @throws JsonProcessingException
     */
    private void updateStockDetailWithDividend(Stock stock, double totalDividend) throws JsonProcessingException {
        StockDetail detail = stockDetailRepository.findById(stock.getId())
                .orElse(null);
        if (detail == null) {
            log.warn("[DIV] StockDetail 없음 - symbol: {}", stock.getSymbol());
            return;
        }

        ObjectNode financialDataNode = (detail.getFinancialData() != null)
                ? (ObjectNode) objectMapper.readTree(detail.getFinancialData())
                : objectMapper.createObjectNode();

        String existing = financialDataNode.path("dividend_amount").asText();
        String newAmountStr = String.format("%.0f", totalDividend);

        if (existing.equals(newAmountStr)) {
            log.info("[DIV] 배당금 동일 ({}원), 업데이트 생략 - symbol: {}", newAmountStr, stock.getSymbol());
            return;
        }

        financialDataNode.put("dividend_amount", newAmountStr);
        detail.setFinancialData(financialDataNode.toString());
        stockDetailRepository.save(detail);

        log.info("[DIV] 배당금 업데이트 완료 - symbol: {}, {}원", stock.getSymbol(), newAmountStr);
    }

    /**
     * 주가와 배당금 기준으로 배당수익률을 계산합니다.
     * 배당금 또는 가격 정보가 없거나 잘못된 경우 0.0을 반환합니다.
     *
     * @param detail
     * @return
     */
    public Double calculateDividendYield(StockDetail detail) {
        if (detail == null || detail.getFinancialData() == null) {
            log.warn("[DIV] StockDetail 또는 FinancialData가 null - id: {}",
                    detail != null ? detail.getId() : "null");
            return null;
        }

        Double price = detail.getPrice();
        if (price == null || price <= 0) {
            log.warn("[DIV] 주가 정보 누락 또는 유효하지 않음 - id: {}, price: {}",
                    detail.getId(), price);
            return null;
        }

        try {
            JsonNode data = objectMapper.readTree(detail.getFinancialData());
            Double dividend = toDouble(data.path("dividend_amount").asText());

            if (dividend == null || dividend <= 0) {
                log.info("[DIV] 유효한 배당금 없음 - id: {}, dividend: {}", detail.getId(), dividend);
                return null;
            }

            double result = (dividend / price) * 100;
            return Math.round(result * 100.0) / 100.0; // 소수점 둘째 자리 반올림

        } catch (Exception e) {
            log.error("[DIV] 배당수익률 계산 중 예외 발생 - id: {}", detail.getId(), e);
            return null;
        }
    }

    /**
     * 특정 종목의 연간 배당일정 데이터를 요청합니다.
     * KIS dividend API (TR_ID: HHKDB669102C0) 호출.
     *
     * @param symbol
     * @return
     */
    private ObjectNode fetchDividendSchedule(String symbol) {
        String lastlastYear = String.valueOf(LocalDate.now().getYear() - 2);
        String currentYear = String.valueOf(LocalDate.now().getYear());
        String from = lastlastYear + "0101";   // ex: 20230101
        String to = currentYear + "1231";     // ex: 20251231

        return (ObjectNode) kisApiClient.get(
                "/uapi/domestic-stock/v1/ksdinfo/dividend",
                Map.of(
                        "cts", "",
                        "gb1", "0",
                        "f_dt", from,
                        "t_dt", to,
                        "sht_cd", symbol,
                        "high_gb", ""
                ),
                "HHKDB669102C0"
        );
    }
}