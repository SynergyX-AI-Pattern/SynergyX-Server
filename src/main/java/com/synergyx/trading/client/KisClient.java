package com.synergyx.trading.client;

import com.synergyx.trading.config.KisProperties;
import com.synergyx.trading.dto.kis.KisResponseWrapper;
import com.synergyx.trading.dto.kis.KisStockDTO;
import com.synergyx.trading.dto.kis.KisTokenRequest;
import com.synergyx.trading.dto.kis.KisTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KisClient {

    private final WebClient webClient;
    private final KisProperties kisProperties;

    public KisTokenResponse fetchToken() {
        KisTokenRequest request = KisTokenRequest.builder()
                .grant_type("client_credentials")
                .appkey(kisProperties.getAppKey())
                .appsecret(kisProperties.getAppSecret())
                .build();

        return webClient.post()
                .uri("/oauth2/tokenP")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(KisTokenResponse.class)
                .block();
    }

    public void fetchAndSaveStockData(String accessToken) {
//        webClient.get()
//                .uri("/uapi/domestic-stock/v1/quotations/inquire-price?fid_cond_mrkt_div_code=J&fid_input_iscd=005930")
//                .header("Authorization", "Bearer " + accessToken)
//                .retrieve()
//                .bodyToMono(SomeStockDTO.class)  //todo: dto 매핑
//                .doOnNext(dto -> {
//                     repository.save(dto.toEntity()); // db 저장
//                })
//                .block();
    }

// todo: 수정된 코드에 맞게 100종목 가져오는 코드 수정

//    public List<KisStockDTO> getKospi100Stocks() {
//        return webClient.get()
//                .uri("/uapi/domestic-stock/v1/quotations/inquire-daily-item-price?fid_cond_mrkt_div_code=J&fid_input_iscd=005930") // 예시: 삼성전자
//                .header("authorization", "Bearer " + kisProperties.getAccessToken())
//                .header("appkey", kisProperties.getAppKey())
//                .header("appsecret", kisProperties.getAppSecret())
//                .header("tr_id", "FHKST01010100")
//                .retrieve()
//                .bodyToMono(KisResponseWrapper.class)
//                .map(KisResponseWrapper::toDtoList)
//                .block();
//    }
}
