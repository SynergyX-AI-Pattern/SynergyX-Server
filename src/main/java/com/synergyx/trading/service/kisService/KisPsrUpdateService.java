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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static com.synergyx.trading.util.ParsingUtil.toDouble;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisPsrUpdateService {

    private final KisApiClient kisApiClient;
    private final StockRepository stockRepository;
    private final StockDetailRepository stockDetailRepository;
    private final ObjectMapper objectMapper;

    /**
     * 전체 PSR 을 업데이트합니다.
     */
    @Transactional
    public void updatePsr() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            try {
                updatePsrInternal(stock);
                Thread.sleep(200);
            } catch (Exception e) {
                log.error("[PSR] {} PSR 업데이트 실패: {}", stock.getSymbol(), e.getMessage(), e);
            }
        }
    }

    /**
     * 개별 종목의 PSR 을 업데이트 합니다.
     *
     * @param symbol 종목 코드
     */
    @Transactional
    public void updatePsrBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("해당 종목 없음: " + symbol));

        try {
            updatePsrInternal(stock);
        } catch (Exception e) {
            log.error("[PSR] {} PSR 업데이트 실패: {}", symbol, e.getMessage(), e);
        }
    }

    /**
     * PSR 을 계산하여 저장합니다.
     *
     * @param stock
     * @throws Exception
     */
    private void updatePsrInternal(Stock stock) throws Exception {
        ObjectNode response = fetchIncomeStatement(stock.getSymbol());

//        log.info("[PSR] 응답 전체 JSON ({}):\n{}", stock.getSymbol(),
//                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

        // 최신 연간 보고서(stac_yymm 끝이 12) 중 가장 최근 데이터 선택
        JsonNode latestAnnual = StreamSupport.stream(response.path("output").spliterator(), false)
                .filter(node -> node.has("stac_yymm") && node.get("stac_yymm").asText().endsWith("12"))
                .max(Comparator.comparing(node -> node.get("stac_yymm").asText()))
                .orElse(null);

        if (latestAnnual == null) {
            log.warn("[PSR] 유효한 연간 데이터가 없습니다. symbol: {}", stock.getSymbol());
            return;
        }

        Double sales = toDouble(latestAnnual.path("sale_account").asText());
//        log.info("[PSR] 최신 연간 보고서 기준 Sales ({}): {}", latestAnnual.get("stac_yymm").asText(), sales);

        if (sales == null || sales == 0) {
            log.warn("[PSR] PSR 계산 실패 - symbol: {}, sales: {}", stock.getSymbol(), sales);
            return;
        }

        StockDetail detail = stockDetailRepository.findById(stock.getId()).orElse(null);
        if (detail == null) {
            log.warn("[PSR] StockDetail 없음 - symbol: {}", stock.getSymbol());
            return;
        }

        ObjectNode financialDataNode = (detail.getFinancialData() != null)
                ? (ObjectNode) objectMapper.readTree(detail.getFinancialData())
                : objectMapper.createObjectNode();

        Double marketCap = toDouble(financialDataNode.path("market_cap").asText());
        if (marketCap == null || marketCap == 0) {
            log.warn("[PSR] market_cap 없음 또는 0 - symbol: {}", stock.getSymbol());
            return;
        }

        double psr = marketCap / sales;

        String newPsrStr = String.format("%.3f", psr);
        String existingPsrStr = financialDataNode.path("psr").asText();

        if (existingPsrStr != null && existingPsrStr.equals(newPsrStr)) {
            log.info("[KIS] PSR 값 동일 ({}), 업데이트 생략. symbol: {}", newPsrStr, stock.getSymbol());
            return;
        }

        financialDataNode.put("psr", newPsrStr);
        detail.setFinancialData(financialDataNode.toString());
        stockDetailRepository.save(detail);

        log.info("[PSR] {} PSR 업데이트 완료: {}", stock.getSymbol(), newPsrStr);
    }

    /**
     * 특정 종목의 손익계산서 데이터를 요청합니다.
     * KIS income-statement API (TR_ID: FHKST66430200) 호출.
     *
     * @param symbol 종목 코드
     * @return 손익계산서 응답의 JSON 객체
     */
    private ObjectNode fetchIncomeStatement(String symbol) {
        return (ObjectNode) kisApiClient.get(
                "/uapi/domestic-stock/v1/finance/income-statement",
                Map.of(
                        "FID_DIV_CLS_CODE", "0",
                        "fid_cond_mrkt_div_code", "J",
                        "fid_input_iscd", symbol
                ),
                "FHKST66430200"
        );
    }
}