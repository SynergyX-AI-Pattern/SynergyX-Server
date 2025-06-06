package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synergyx.trading.config.KisProperties;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockDetail;
import com.synergyx.trading.repository.StockDetailRepository;
import com.synergyx.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisStockDetailService {

    private final WebClient kisWebClient;
    private final KisProperties kisProperties;
    private final StockRepository stockRepository;
    private final StockDetailRepository stockDetailRepository;
    private final ObjectMapper objectMapper;
    private final KisTokenService tokenService;

    /**
     * 시가총액/현재가/등락률 + PER/PBR 을 업데이트합니다.
     */
    @Transactional
    public void updateStockDetailsFromKis() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            try {
                String accessToken = tokenService.getAccessToken();
                ObjectNode jsonNode = fetchPriceJson(stock.getSymbol(), accessToken);

                StockDetail detail = createOrUpdateStockDetail(stock, jsonNode);
                boolean isNew = (detail.getUpdatedAt() == null); // 예시 조건

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
            String accessToken = tokenService.getAccessToken();
            ObjectNode jsonNode = fetchPriceJson(symbol, accessToken);

            StockDetail detail = createOrUpdateStockDetail(stock, jsonNode);
            boolean isNew = (detail.getUpdatedAt() == null); // 또는 직접 DB에서 존재 여부 체크해도 됨

            stockDetailRepository.save(detail);
            log.info("[KIS] 종목({}) {} 처리 완료", symbol, isNew ? "신규 등록" : "업데이트");

        } catch (Exception e) {
            log.error("[KIS] Failed to update stock detail for {}: {}", symbol, e.getMessage(), e);
        }
    }

    private ObjectNode fetchPriceJson(String symbol, String token) throws JsonProcessingException {
        String response = kisWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/uapi/domestic-stock/v1/quotations/inquire-price")
                        .queryParam("fid_cond_mrkt_div_code", "J")
                        .queryParam("fid_input_iscd", symbol)
                        .build())
                .headers(headers -> {
                    headers.setBearerAuth(token);
                    headers.set("appkey", kisProperties.getAppKey());
                    headers.set("appsecret", kisProperties.getAppSecret());
                    headers.set("tr_id", "FHKST01010100");
                    headers.set("custtype", "P");
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return (ObjectNode) objectMapper.readTree(response).get("output");
    }

    private StockDetail createOrUpdateStockDetail(Stock stock, ObjectNode jsonNode) throws JsonProcessingException {
        Double price = parseDouble(jsonNode.get("stck_prpr").asText());
        Double changeRate = parseDouble(jsonNode.get("prdy_ctrt").asText());

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
        stockDetail.setChangeRate(changeRate);
        stockDetail.setFinancialData(financialDataNode.toString());

        return stockDetail;
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }
}