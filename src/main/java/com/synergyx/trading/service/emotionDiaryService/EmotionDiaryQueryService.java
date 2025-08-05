package com.synergyx.trading.service.emotionDiaryService;
import com.synergyx.trading.dto.emotionDiary.EmotionDiaryResponseDTO;
import java.util.List;

public interface EmotionDiaryQueryService {
    // 감정 투자 일기 전체 목록 조회
    List<EmotionDiaryResponseDTO.EmotionDiaryDTO> getEmotionDiaryList(Long userId);
}
