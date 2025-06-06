package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synergyx.trading.config.KisProperties;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockDetail;
import com.synergyx.trading.repository.StockDetailRepository;
import com.synergyx.trading.repository.StockOhlcvRepository;
import com.synergyx.trading.repository.StockRepository;
import jakarta.persistence.EntityNotFoundException;
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
     * 시가총액/현재가/등락률 + PER/PBR 을 업데이트합니다.
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

                // 시세, 등락률
                Double price = parseDouble(jsonNode.get("stck_prpr").asText());
                Double changeRate = parseDouble(jsonNode.get("prdy_ctrt").asText());

                StockDetail stockDetail = stockDetailRepository.findById(stock.getId()).orElse(null);
                ObjectNode financialDataNode = (stockDetail != null && stockDetail.getFinancialData() != null)
                        ? (ObjectNode) objectMapper.readTree(stockDetail.getFinancialData())
                        : objectMapper.createObjectNode();

                // hts 시가총액
                financialDataNode.put("market_cap", jsonNode.get("hts_avls").asText());

                String newPer = jsonNode.get("per").asText();
                String newPbr = jsonNode.get("pbr").asText();

                if (!newPer.equals(financialDataNode.path("per").asText()) ||
                        !newPbr.equals(financialDataNode.path("pbr").asText())) {
                    financialDataNode.put("per", newPer);
                    financialDataNode.put("pbr", newPbr);
                }

                StockDetail newDetail = StockDetail.builder()
                        .stock(stock)
                        .price(price)
                        .changeRate(changeRate)
                        .financialData(financialDataNode.toString())
                        .build();

                stockDetailRepository.save(newDetail);
                Thread.sleep(200); // api 호출 제한으로 대기 (1초에 20회)

            } catch (Exception e) {
                log.error("[KIS] Failed to fetch/update stock detail for {}: {}", stock.getSymbol(), e.getMessage(), e);
            }
        }
    }

    /**
     * ROE 를 업데이트합니다.
     */
    @Transactional
    public void updateRoeFromKis() {
        List<StockDetail> details = stockDetailRepository.findAll();

        for (StockDetail detail : details) {
            try {
                String accessToken = tokenService.getAccessToken();
                String response = kisWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/uapi/domestic-stock/v1/finance/financial-ratio")
                                .queryParam("fid_div_cls_code", "1")
                                .queryParam("fid_cond_mrkt_div_code", "J")
                                .queryParam("fid_input_iscd", detail.getStock().getSymbol())
                                .build())
                        .header("authorization", "Bearer " + accessToken)
                        .header("appkey", kisProperties.getAppKey())
                        .header("appsecret", kisProperties.getAppSecret())
                        .header("tr_id", "FHKST66430300")
                        .header("custtype", "P")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

//                log.info("[KIS] Raw response for stock {}: {}", detail.getStock().getSymbol(), response);

                // 전체 응답 데이터
                JsonNode root = objectMapper.readTree(response);
                JsonNode outputArray = root.path("output");

                if (!outputArray.isArray() || outputArray.size() == 0) {
                    log.warn("[KIS] 'output' is not a valid array or is empty for stock {}. msg1: {}", detail.getStock().getSymbol(), root.path("msg1").asText());
                    return;
                }

                // 최신 분기 데이터 가져오기
                JsonNode latest = outputArray.get(0);
                String roe = latest.path("roe_val").asText();
                String stacYymm = latest.path("stac_yymm").asText();

//                log.info("[KIS] ROE for stock {} (기준일자 {}): {}", detail.getStock().getSymbol(), stacYymm, roe);

                // JSON 필드 추가
                ObjectNode financialDataNode = (ObjectNode) objectMapper.readTree(detail.getFinancialData());

                // 기존 값과 비교
                String existingRoe = financialDataNode.has("roe") ? financialDataNode.path("roe").asText() : null;

                if (existingRoe != null && existingRoe.equals(roe)) {
//                    log.info("[KIS] ROE 값 동일 ({}), 업데이트 생략. stock: {}", roe, detail.getStock().getSymbol());
                    return;
                }

                financialDataNode.put("roe", roe);
                detail.setFinancialData(financialDataNode.toString());

                // roe 저장
                stockDetailRepository.save(detail);
//                log.info("[KIS] ROE value saved for stock {}", detail.getStock().getSymbol());

                Thread.sleep(200); // api 호출 제한으로 대기 (1초에 20회)
            } catch (Exception e) {
                log.warn("[KIS] Failed to update ROE for {}: {}", detail.getStock().getSymbol(), e.getMessage());
            }
        }
    }

    /**
     * 개별 종목의 ROE 를 업데이트 합니다.
     */
    @Transactional
    public void updateRoeFromKisTest(String stockCode) {
        try {
            String accessToken = tokenService.getAccessToken();
            String response = kisWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/uapi/domestic-stock/v1/finance/financial-ratio")
                            .queryParam("fid_div_cls_code", "1")  // 분기 기준
                            .queryParam("fid_cond_mrkt_div_code", "J")
                            .queryParam("fid_input_iscd", stockCode)
                            .build())
                    .header("authorization", "Bearer " + accessToken)
                    .header("appkey", kisProperties.getAppKey())
                    .header("appsecret", kisProperties.getAppSecret())
                    .header("tr_id", "FHKST66430300")
                    .header("custtype", "P")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("[KIS] Raw response for stock {}: {}", stockCode, response);

            // 전체 응답 데이터
            JsonNode root = objectMapper.readTree(response);
            JsonNode outputArray = root.path("output");

            if (!outputArray.isArray() || outputArray.size() == 0) {
                log.warn("[KIS] 'output' is not a valid array or is empty for stock {}. msg1: {}", stockCode, root.path("msg1").asText());
                return;
            }

            // 최신 분기 데이터 가져오기
            JsonNode latest = outputArray.get(0);
            String roe = latest.path("roe_val").asText();
            String stacYymm = latest.path("stac_yymm").asText();

            log.info("[KIS] ROE for stock {} (기준일자 {}): {}", stockCode, stacYymm, roe);

            StockDetail stockDetail = stockDetailRepository.findByStock_Symbol(stockCode)
                    .orElseThrow(() -> new EntityNotFoundException("StockDetail not found for symbol: " + stockCode));
            if (stockDetail == null) {
                log.warn("[KIS] No stock detail found for stock: {}", stockCode);
                return;
            }

            ObjectNode financialDataNode = (ObjectNode) objectMapper.readTree(stockDetail.getFinancialData());

            // 기존 값과 비교
            String existingRoe = financialDataNode.has("roe") ? financialDataNode.path("roe").asText() : null;

            if (existingRoe != null && existingRoe.equals(roe)) {
                log.info("[KIS] ROE 값 동일 ({}), 업데이트 생략. stock: {}", roe, stockCode);
                return;
            }

            financialDataNode.put("roe", roe);
            stockDetail.setFinancialData(financialDataNode.toString());

            // roe 저장
            stockDetailRepository.save(stockDetail);
            log.info("[KIS] ROE value saved for stock {}", stockCode);

        } catch (Exception e) {
            log.warn("[KIS] Failed to update ROE for {}: {}", stockCode, e.getMessage(), e);
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