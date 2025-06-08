package com.synergyx.trading.service.kisService;

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

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisDividendScheduleService {

    private final KisApiClient kisApiClient;
    private final StockRepository stockRepository;
    private final StockDetailRepository stockDetailRepository;
    private final ObjectMapper objectMapper;

    /**
     * 전체 종목의 배당수익률 (dividend_yield)을 업데이트합니다.
     */
    @Transactional
    public void updateDividendYieldAll() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            try {
                updateDividendYieldInternal(stock);
                Thread.sleep(200); // API 호출 제한
            } catch (Exception e) {
                log.error("[DIV] {} 배당수익률 업데이트 실패: {}", stock.getSymbol(), e.getMessage(), e);
            }
        }
    }

    /**
     * 개별 종목의 배당수익률 (dividend_yield)을 업데이트합니다.
     */
    @Transactional
    public void updateDividendYieldBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("해당 종목 없음: " + symbol));

        try {
            updateDividendYieldInternal(stock);
        } catch (Exception e) {
            log.error("[DIV] {} 배당수익률 업데이트 실패: {}", symbol, e.getMessage(), e);
        }
    }

    /**
     * 배당수익률을 저장합니다.
     *
     * @param stock
     * @throws Exception
     */
    private void updateDividendYieldInternal(Stock stock) throws Exception {
        ObjectNode response = fetchDividendSchedule(stock.getSymbol());

//        log.info("[PSR] 응답 전체 JSON ({}):\n{}", stock.getSymbol(), objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

        JsonNode output = response.get("output1");
        if (output == null || !output.isArray()) {
            log.warn("[DIV] 응답에 output1 없음 - symbol: {}", stock.getSymbol());
            return;
        }

        // 결산 데이터 필터링
        JsonNode match = StreamSupport.stream(output.spliterator(), false)
                .filter(node -> stock.getSymbol().equals(node.path("sht_cd").asText()))
                .filter(node -> "결산".equals(node.path("divi_kind").asText()))
                .filter(node -> {
                    String recordDate = node.path("record_date").asText();
                    return recordDate != null && recordDate.length() >= 6 && recordDate.substring(4, 6).equals("12");
                })
                .findFirst()
                .orElse(null);

        if (match == null) {
            log.warn("[DIV] 조건에 맞는 결산 배당 데이터 없음 - symbol: {}", stock.getSymbol());
            return;
        }

        Double dividendAmount = toDouble(match.path("per_sto_divi_amt").asText());
        if (dividendAmount == null || dividendAmount <= 0) {
            log.warn("[DIV] 유효하지 않은 현금배당금 - symbol: {}, value: {}", stock.getSymbol(), dividendAmount);
            return;
        }

        StockDetail detail = stockDetailRepository.findById(stock.getId()).orElse(null);
        if (detail == null) {
            log.warn("[DIV] StockDetail 없음 - symbol: {}", stock.getSymbol());
            return;
        }

        Double price = detail.getPrice();
        if (price == null || price <= 0) {
            log.warn("[DIV] 현재가 정보 없음 또는 0 이하 - symbol: {}, price: {}", stock.getSymbol(), price);
            return;
        }

        // 배당수익률 계산
        double dividendYield = (dividendAmount / price) * 100;
        String newYieldStr = String.format("%.2f", dividendYield);

        ObjectNode financialDataNode = (detail.getFinancialData() != null)
                ? (ObjectNode) objectMapper.readTree(detail.getFinancialData())
                : objectMapper.createObjectNode();

        String existingYield = financialDataNode.path("dividend_yield").asText().strip();

        if (existingYield.equals(newYieldStr)) {
            log.info("[DIV] 배당수익률 동일 ({}%), 업데이트 생략 - symbol: {}", newYieldStr, stock.getSymbol());
            return;
        }

        financialDataNode.put("dividend_yield", newYieldStr);
        detail.setFinancialData(financialDataNode.toString());
        stockDetailRepository.save(detail);

        log.info("[DIV] 배당수익률 업데이트 완료 - symbol: {}, {}%", stock.getSymbol(), newYieldStr);
    }

    /**
     * 특정 종목의 배당일정 데이터를 요청합니다.
     * KIS dividend API (TR_ID: HHKDB669102C0) 호출.
     *
     * @param symbol
     * @return
     */
    private ObjectNode fetchDividendSchedule(String symbol) {
        return (ObjectNode) kisApiClient.get(
                "/uapi/domestic-stock/v1/ksdinfo/dividend",
                Map.of(
                        "cts1", "",
                        "gb1", "0",
                        "f_dt", "20240930",
                        "t_dt", "20241231",
                        "sht_cd", symbol,
                        "high_gb", "0"
                ),
                "HHKDB669102C0"
        );
    }
}