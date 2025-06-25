package com.synergyx.trading.service.backtestService.client;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.backtest.BacktestRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.synergyx.trading.dto.backtest.BacktestResponseDTO;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BacktestClientService {

    private final WebClient fastApiWebClient;

    public BacktestResponseDTO.BacktestExecutionDTO callBacktestAPI(Long patternId, Long stockId, LocalDate startDate, LocalDate endDate) {
        BacktestRequestDTO request = BacktestRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        try {
            BacktestResponseDTO.BacktestWrapperResponseDTO response = fastApiWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/backtests")
                            .queryParam("patternId", patternId)
                            .queryParam("stockId", stockId)
                            .build())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(BacktestResponseDTO.BacktestWrapperResponseDTO.class)
                    .block();

            if (response == null || !response.isSuccess()) {
                throw new GeneralException(ErrorStatus.FASTAPI_BACKTEST_FAILED);
            }

            return response.getData();

        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.FASTAPI_BACKTEST_FAILED);
        }
    }
}

