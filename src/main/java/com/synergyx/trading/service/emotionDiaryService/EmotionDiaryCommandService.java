package com.synergyx.trading.service.emotionDiaryService;

import com.synergyx.trading.dto.emotionDiary.EmotionDiaryResponseDTO;
import com.synergyx.trading.dto.emotionDiary.EmotionDiaryRequestDTO;

public interface EmotionDiaryCommandService {
    // 감정 투자 일기 작성
    EmotionDiaryResponseDTO.EmotionDiaryDTO writeDiary(Long userId, EmotionDiaryRequestDTO dto);

    // 감정 투자 일기 삭제
    void deleteDiary(Long userId, Long diaryId);
}