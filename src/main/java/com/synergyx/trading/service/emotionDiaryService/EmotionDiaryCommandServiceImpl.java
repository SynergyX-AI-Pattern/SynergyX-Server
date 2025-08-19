package com.synergyx.trading.service.emotionDiaryService;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.emotionDiary.EmotionDiaryRequestDTO;
import com.synergyx.trading.dto.emotionDiary.EmotionDiaryResponseDTO;
import com.synergyx.trading.model.*;
import com.synergyx.trading.repository.*;
import com.synergyx.trading.service.emotionDiaryService.client.EmotionDiaryClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmotionDiaryCommandServiceImpl implements  EmotionDiaryCommandService {

    private final EmotionDiaryRepository emotionDiaryRepository;
    private final UserRepository userRepository;
    private final EmotionDiaryClientService emotionDiaryClientService;

    // 감정 투자 일기 생성
    @Override
    @Transactional
    public EmotionDiaryResponseDTO.EmotionDiaryDTO writeDiary(Long userId, EmotionDiaryRequestDTO dto) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 빈 칸 예외 처리
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new GeneralException(ErrorStatus.INVALID_CONTENT);
        }

        EmotionDiaryResponseDTO.EmotionAnalysisResultDTO result = emotionDiaryClientService.callEmotionDiaryAPI(dto.getContent());

        EmotionDiary diary = emotionDiaryRepository.save(
                EmotionDiary.builder()
                        .user(user)
                        .content(dto.getContent())
                        .emotion(result.getEmotion())
                        .summary(result.getSummary())
                        .feedback(result.getFeedback())
                        .build()
        );

        return EmotionDiaryResponseDTO.EmotionDiaryDTO.builder()
                .diaryId(diary.getId())
                .content(diary.getContent())
                .emotion(diary.getEmotion())
                .summary(diary.getSummary())
                .feedback(diary.getFeedback())
                .createdAt(diary.getCreatedAt())
                .build();
    }

}