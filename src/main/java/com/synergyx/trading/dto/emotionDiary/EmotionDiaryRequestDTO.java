package com.synergyx.trading.dto.emotionDiary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 감정 일기 생성
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionDiaryRequestDTO {
    private String content; // 일기 원문
}