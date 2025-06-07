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

import java.util.List;
import java.util.Map;

import static com.synergyx.trading.util.ParsingUtil.toDouble;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisPsrUpdateService {

    private final KisApiClient kisApiClient;
    private final StockRepository stockRepository;
    private final StockDetailRepository stockDetailRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void updatePsr() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            try {
                ObjectNode response = fetchIncomeStatement(stock.getSymbol());
                JsonNode firstReport = response.path("output").get(0);

                String saleAccountText = firstReport.path("sale_account").asText();
                Double sales = toDouble(saleAccountText);

                StockDetail detail = stockDetailRepository.findById(stock.getId())
                        .orElse(null);

                if (detail == null || sales == null || sales == 0) {
                    log.warn("[PSR] PSR 계산 실패 - symbol: {}, sales: {}", stock.getSymbol(), sales);
                    continue;
                }

                ObjectNode financialDataNode = (detail.getFinancialData() != null)
                        ? (ObjectNode) objectMapper.readTree(detail.getFinancialData())
                        : objectMapper.createObjectNode();

                Double marketCap = toDouble(financialDataNode.path("market_cap").asText());
                if (marketCap == null) {
                    log.info("[PSR] market_cap 없음 - symbol: {}", stock.getSymbol());
                    continue;
                }

                double psr = marketCap / sales;
                String newPsrStr = String.format("%.3f", psr);
                String existingPsrStr = financialDataNode.path("psr").asText();

                if (existingPsrStr != null && existingPsrStr.equals(newPsrStr)) {
                    log.info("[KIS] PSR 값 동일 ({}), 업데이트 생략. symbol: {}", newPsrStr, stock.getSymbol());
                    continue;
                }

                // 변경된 경우 저장
                financialDataNode.put("psr", newPsrStr);
                detail.setFinancialData(financialDataNode.toString());
                stockDetailRepository.save(detail);

                log.info("[PSR] {} PSR 업데이트 완료: {}", stock.getSymbol(), newPsrStr);

                Thread.sleep(200); // 호출 제한

            } catch (Exception e) {
                log.error("[PSR] {} PSR 업데이트 실패: {}", stock.getSymbol(), e.getMessage(), e);
            }
        }
    }

    @Transactional
    public void updatePsrBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("해당 종목 없음: " + symbol));

        try {
            ObjectNode response = fetchIncomeStatement(symbol);
            JsonNode firstReport = response.path("output").get(0);

            String saleAccountText = firstReport.path("sale_account").asText();
            Double sales = toDouble(saleAccountText);

            StockDetail detail = stockDetailRepository.findById(stock.getId())
                    .orElse(null);

            if (detail == null || sales == null || sales == 0) {
                log.info("[PSR] PSR 계산 실패 - symbol: {}, sales: {}", symbol, sales);
                return;
            }

            ObjectNode financialDataNode = (detail.getFinancialData() != null)
                    ? (ObjectNode) objectMapper.readTree(detail.getFinancialData())
                    : objectMapper.createObjectNode();

            Double marketCap = toDouble(financialDataNode.path("market_cap").asText());
            if (marketCap == null) {
                log.info("[PSR] market_cap 없음 - symbol: {}", symbol);
                return;
            }

            double psr = marketCap / sales;
            String newPsrStr = String.format("%.3f", psr);
            String existingPsrStr = financialDataNode.path("psr").asText();

            if (existingPsrStr != null && existingPsrStr.equals(newPsrStr)) {
                log.info("[KIS] PSR 값 동일 ({}), 업데이트 생략. symbol: {}", newPsrStr, symbol);
                return;
            }

            // 변경된 경우 저장
            financialDataNode.put("psr", newPsrStr);
            detail.setFinancialData(financialDataNode.toString());
            stockDetailRepository.save(detail);

            log.info("[PSR] {} PSR 업데이트 완료: {}", symbol, newPsrStr);

        } catch (Exception e) {
            log.error("[PSR] {} PSR 업데이트 실패: {}", symbol, e.getMessage(), e);
        }
    }

    private ObjectNode fetchIncomeStatement(String symbol) {
        return (ObjectNode) kisApiClient.get(
                "/uapi/domestic-stock/v1/finance/income-statement",
                Map.of(
                        "FID_DIV_CLS_CODE", "1",
                        "fid_cond_mrkt_div_code", "J",
                        "fid_input_iscd", symbol
                ),
                "FHKST66430200"
        );
    }
}
