package com.synergyx.trading.client;

import com.synergyx.trading.config.KisProperties;
import com.synergyx.trading.dto.kis.KisResponseWrapper;
import com.synergyx.trading.dto.kis.KisStockDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KisClient {

    private final WebClient webClient;
    private final KisProperties kisProperties;

    public List<KisStockDTO> getKospi100Stocks() {
        return webClient.get()
                .uri("/uapi/domestic-stock/v1/quotations/inquire-daily-item-price?fid_cond_mrkt_div_code=J&fid_input_iscd=005930") // 예시: 삼성전자
                .header("authorization", "Bearer " + kisProperties.getAccessToken())
                .header("appkey", kisProperties.getAppKey())
                .header("appsecret", kisProperties.getAppSecret())
                .header("tr_id", "FHKST01010100")
                .retrieve()
                .bodyToMono(KisResponseWrapper.class)
                .map(KisResponseWrapper::toDtoList)
                .block();
    }
}
