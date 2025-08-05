package com.synergyx.trading.service.emotionDiaryService;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.emotionDiary.EmotionDiaryRequestDTO;
import com.synergyx.trading.dto.emotionDiary.EmotionDiaryResponseDTO;
import com.synergyx.trading.model.*;
import com.synergyx.trading.repository.*;
//import com.synergyx.trading.service.emotionDiaryService.client.EmotionDiaryClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmotionDiaryCommandServiceImpl implements  EmotionDiaryCommandService {

    private final EmotionDiaryRepository emotionDiaryRepository;
    private final UserRepository userRepository;
//    private final EmotionDiaryClientService emotionDiaryClientService;

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

        // TODO: fastAPI 연결 후 주석 삭제
//        EmotionDiaryResponseDTO.EmotionAnalysisResultDTO result = emotionDiaryClientService.callEmotionDiaryAPI(dto.getContent());
//
//        EmotionDiary diary = emotionDiaryRepository.save(
//                EmotionDiary.builder()
//                        .user(user)
//                        .content(dto.getContent())
//                        .emotion(result.getEmotion())
//                        .summary(result.getSummary())
//                        .feedback(result.getFeedback())
//                        .build()
//        );

        // TODO: fastAPI 연결 후 삭제
        // 목데이터
        EmotionDiary diary = emotionDiaryRepository.save(
                EmotionDiary.builder()
                        .user(user)
                        .content(dto.getContent())
                        .emotion(List.of("기쁨", "만족", "뿌듯함"))
                        .summary("주가 급등으로 인한 큰 수익과 긍정적인 감정 표현")
                        .feedback("수익 실현 축하드려요! 앞으로도 꾸준한 리스크 관리와 분산 투자를 유지해보세요.")
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