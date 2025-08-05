package com.synergyx.trading.dto.emotionDiary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class EmotionDiaryResponseDTO {

    // FastAPI에서 받은 응답을 감싸는 Wrapper DTO
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionDiaryWrapperResponseDTO {

        // camelCase로 매핑
        @JsonProperty("is_success")
        private boolean isSuccess;

        private String code;
        private String message;
        private EmotionDiaryResponseDTO.EmotionAnalysisResultDTO data;
    }

    // 감정 일기 생성, 전체 목록 조회
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmotionDiaryDTO {
        private Long diaryId; // 일기 id
        private String content; // 원문
        private List<String> emotion; // 감정
        private String summary; // 요약
        private String feedback; // 조언
        private LocalDateTime createdAt; // 작성 일시
    }

    // 감정 분석 결과
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmotionAnalysisResultDTO  {
        private List<String> emotion; // 감정
        private String summary; // 요약
        private String feedback; // 조언
    }
}
