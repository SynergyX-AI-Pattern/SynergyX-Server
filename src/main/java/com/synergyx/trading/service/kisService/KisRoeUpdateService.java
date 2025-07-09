package com.synergyx.trading.service.kisService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.synergyx.trading.model.StockDetail;
import com.synergyx.trading.repository.StockDetailRepository;
import com.synergyx.trading.service.kisService.client.KisApiClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KisRoeUpdateService {

    private final KisApiClient kisApiClient;
    private final StockDetailRepository stockDetailRepository;
    private final ObjectMapper objectMapper;
    private final KisTokenService tokenService;

    private static final int REQUEST_INTERVAL_MILLIS = 400;

    /**
     * ROE 를 업데이트합니다.
     */
    @Transactional
    public void updateRoeFromKis() {
        List<StockDetail> details = stockDetailRepository.findAll();

        for (StockDetail detail : details) {
            try {
                JsonNode response = kisApiClient.get(
                        "/uapi/domestic-stock/v1/finance/financial-ratio",
                        Map.of(
                                "fid_div_cls_code", "1",
                                "fid_cond_mrkt_div_code", "J",
                                "fid_input_iscd", detail.getStock().getSymbol()
                        ),
                        "FHKST66430300"
                );

//                log.info("[KIS] Raw response for stock {}: {}", detail.getStock().getSymbol(), response);

                // 전체 응답 데이터
                JsonNode outputArray = response.path("output");

                if (!outputArray.isArray() || outputArray.size() == 0) {
                    log.warn("[KIS] 'output' is not a valid array or is empty for stock {}. msg1: {}", detail.getStock().getSymbol(), response.path("msg1").asText());
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

                Thread.sleep(REQUEST_INTERVAL_MILLIS); // API 호출 제한
            } catch (Exception e) {
                log.warn("[KIS] Failed to update ROE for {}: {}", detail.getStock().getSymbol(), e.getMessage());
            }
        }
    }

    /**
     * 개별 종목의 ROE 를 업데이트 합니다.
     */
    @Transactional
    public void updateRoeBySymbol(String stockCode) {
        try {
            JsonNode response = kisApiClient.get(
                    "/uapi/domestic-stock/v1/finance/financial-ratio",
                    Map.of(
                            "fid_div_cls_code", "1",
                            "fid_cond_mrkt_div_code", "J",
                            "fid_input_iscd", stockCode
                    ),
                    "FHKST66430300"
            );

            log.info("[KIS] Raw response for stock {}: {}", stockCode, response);

            // 전체 응답 데이터
            JsonNode outputArray = response.path("output");

            if (!outputArray.isArray() || outputArray.size() == 0) {
                log.warn("[KIS] 'output' is not a valid array or is empty for stock {}. msg1: {}", stockCode, response.path("msg1").asText());
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
}