package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synergyx.trading.config.KisProperties;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockDetail;
import com.synergyx.trading.repository.StockDetailRepository;
import com.synergyx.trading.repository.StockOhlcvRepository;
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
public class KisCommandServiceImpl implements KisCommandService {
    private final WebClient kisWebClient;
    private final KisProperties kisProperties;
    private final StockRepository stockRepository;
    private final StockDetailRepository stockDetailRepository;
    private final StockOhlcvRepository stockOhlcvRepository;
    private final ObjectMapper objectMapper;
    private final KisTokenService tokenService;

    /**
     * KIS 로부터 시가총액, 현재가, 등락율, per, pbr 를 가져와 업데이트합니다. 
     */
    @Transactional
    @Override
    public void updateStockDetailsFromKis() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            try {
                String accessToken = tokenService.getAccessToken();

                String response = kisWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/uapi/domestic-stock/v1/quotations/inquire-price")
                                .queryParam("fid_cond_mrkt_div_code", "J")
                                .queryParam("fid_input_iscd", stock.getSymbol())
                                .build())
                        .header("authorization", "Bearer " + accessToken)
                        .header("appkey", kisProperties.getAppKey())
                        .header("appsecret", kisProperties.getAppSecret())
                        .header("tr_id", "FHKST01010100")
                        .header("custtype", "P")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(response).get("output");

                Double price = parseDouble(jsonNode.get("stck_prpr").asText());
                Double changeRate = parseDouble(jsonNode.get("prdy_ctrt").asText());

                // financial data 생성
                ObjectNode financialDataNode = objectMapper.createObjectNode();
                financialDataNode.put("market_cap", jsonNode.get("hts_avls").asText());
                financialDataNode.put("dividend_yield", ""); // 배당수익률은 아직 없음
                financialDataNode.put("roe", ""); // roe도 나중에 채울 수 있음
                financialDataNode.put("per", jsonNode.get("per").asText());
                financialDataNode.put("pbr", jsonNode.get("pbr").asText());
                financialDataNode.put("psr", ""); // psr은 다른 api에서 제공

                StockDetail stockDetail = StockDetail.builder()
                        .stock(stock)
                        .price(price)
                        .changeRate(changeRate)
                        .financialData(financialDataNode.toString())
                        .build();

                stockDetailRepository.save(stockDetail);
                Thread.sleep(100); // api 호출 제한으로 대기

            } catch (Exception e) {
                log.error("[KIS] Failed to fetch/update stock detail for {}: {}", stock.getSymbol(), e.getMessage(), e);
            }
        }
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }
}
