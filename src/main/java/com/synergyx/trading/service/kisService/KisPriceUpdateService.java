package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockDetail;
import com.synergyx.trading.repository.StockDetailRepository;
import com.synergyx.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.synergyx.trading.service.kisService.client.KisApiClient;

import java.util.List;
import java.util.Map;

import static com.synergyx.trading.util.ParsingUtil.toDouble;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisPriceUpdateService {

    private final KisApiClient kisApiClient;
    private final StockRepository stockRepository;
    private final StockDetailRepository stockDetailRepository;
    private final ObjectMapper objectMapper;

    /**
     * 시가총액/현재가/등락폭/등락률 + PER/PBR 을 업데이트합니다.
     */
    @Transactional
    public void updateStockDetailsFromKis() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            try {
                ObjectNode jsonNode = fetchPriceJson(stock.getSymbol());

                StockDetail detail = createOrUpdateStockDetail(stock, jsonNode);
                boolean isNew = (detail.getUpdatedAt() == null);

                stockDetailRepository.save(detail);
                log.info("[KIS] {} 종목 {}됨", stock.getSymbol(), isNew ? "신규 등록" : "업데이트");
                Thread.sleep(200); // api 호출 제한으로 대기 (1초에 20회)

            } catch (Exception e) {
                log.error("[KIS] Failed to update stock detail for {}: {}", stock.getSymbol(), e.getMessage(), e);
            }
        }
    }

    /**
     * 개별 종목 시세를 업데이트합니다.
     *
     * @param symbol
     */
    @Transactional
    public void updateStockDetailBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("해당 종목코드의 Stock이 존재하지 않음: " + symbol));

        try {
            ObjectNode jsonNode = fetchPriceJson(symbol);

            StockDetail detail = createOrUpdateStockDetail(stock, jsonNode);
            boolean isNew = (detail.getUpdatedAt() == null); // 또는 직접 DB에서 존재 여부 체크해도 됨

            stockDetailRepository.save(detail);
            log.info("[KIS] 종목({}) {} 처리 완료", symbol, isNew ? "신규 등록" : "업데이트");

        } catch (Exception e) {
            log.error("[KIS] Failed to update stock detail for {}: {}", symbol, e.getMessage(), e);
        }
    }

    /**
     * 실제 요청을 수행합니다.
     *
     * @param symbol
     * @return KIS 시세 API 응답의 output 노드
     */
    private ObjectNode fetchPriceJson(String symbol) {
        JsonNode response = kisApiClient.get(
                "/uapi/domestic-stock/v1/quotations/inquire-price",
                Map.of(
                        "fid_cond_mrkt_div_code", "J",
                        "fid_input_iscd", symbol
                ),
                "FHKST01010100"
        );
//        log.info("[Price] 응답 전체 JSON ({}):\n{}", response);

        return (ObjectNode) response.get("output");
    }

    /**
     * Stock Detail 을 생성 또는 업데이트합니다.
     * per, pbr 값이 기존과 다를 때만 변경합니다.
     *
     * @param stock    시세 정보가 연결될 종목 엔티티
     * @param jsonNode KIS 시세 API 응답의 output 노드
     * @return 생성 또는 업데이트된 stockDetail 객체
     * @throws JsonProcessingException
     */
    private StockDetail createOrUpdateStockDetail(Stock stock, ObjectNode jsonNode) throws JsonProcessingException {
        Double price = toDouble(jsonNode.get("stck_prpr").asText()); // 현재가

        String signCode = jsonNode.get("prdy_vrss_sign").asText(); // 등락부호 (1 : 상한, 2 : 상승, 3 : 보합, 4 : 하한, 5 : 하락)
        Double rawChangeAmount = toDouble(jsonNode.get("prdy_vrss").asText()); // 등락폭

        Double signedChangeAmount = switch (signCode) {
            case "1", "2" -> rawChangeAmount;   // 상한 or 상승 → +
            case "4", "5" -> -rawChangeAmount;  // 하한 or 하락 → -
            case "3" -> 0.0;                    // 보합 → 0
            default -> 0.0;
        };

        Double changeRate = toDouble(jsonNode.get("prdy_ctrt").asText()); // 등락률

        StockDetail stockDetail = stockDetailRepository.findById(stock.getId())
                .orElse(StockDetail.builder().stock(stock).build());

        ObjectNode financialDataNode = (stockDetail.getFinancialData() != null)
                ? (ObjectNode) objectMapper.readTree(stockDetail.getFinancialData())
                : objectMapper.createObjectNode();

        financialDataNode.put("market_cap", jsonNode.get("hts_avls").asText());

        String newPer = jsonNode.get("per").asText();
        String newPbr = jsonNode.get("pbr").asText();

        if (!newPer.equals(financialDataNode.path("per").asText())) {
            financialDataNode.put("per", newPer);
        }
        if (!newPbr.equals(financialDataNode.path("pbr").asText())) {
            financialDataNode.put("pbr", newPbr);
        }

        stockDetail.setPrice(price);
        stockDetail.setChangeAmount(signedChangeAmount);
        stockDetail.setChangeRate(changeRate);
        stockDetail.setFinancialData(financialDataNode.toString());

        return stockDetail;
    }
}