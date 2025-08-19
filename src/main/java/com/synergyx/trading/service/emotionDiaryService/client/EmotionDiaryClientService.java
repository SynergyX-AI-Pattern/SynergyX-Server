package com.synergyx.trading.service.emotionDiaryService.client;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.emotionDiary.EmotionDiaryRequestDTO;
import com.synergyx.trading.dto.emotionDiary.EmotionDiaryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class EmotionDiaryClientService {

    private final WebClient fastApiWebClient;

    public EmotionDiaryResponseDTO.EmotionAnalysisResultDTO callEmotionDiaryAPI(String content) {

        EmotionDiaryRequestDTO request = EmotionDiaryRequestDTO.builder()
                .content(content)
                .build();

        try {
            EmotionDiaryResponseDTO.EmotionDiaryWrapperResponseDTO response = fastApiWebClient.post()
                    .uri("/api/v1/diaries")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EmotionDiaryResponseDTO.EmotionDiaryWrapperResponseDTO.class)
                    .block();

            if (response == null || !response.isSuccess()) {
                throw new GeneralException(ErrorStatus.FASTAPI_ANALYSIS_FAILED);
            }

            return response.getData();

        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.FASTAPI_ANALYSIS_FAILED);
        }
    }
}
