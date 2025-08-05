package com.synergyx.trading.service.emotionDiaryService;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.emotionDiary.EmotionDiaryResponseDTO;
import com.synergyx.trading.model.*;
import com.synergyx.trading.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmotionDiaryQueryServiceImpl implements EmotionDiaryQueryService {

    private final UserRepository userRepository;
    private final EmotionDiaryRepository emotionDiaryRepository;

    // 패턴 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<EmotionDiaryResponseDTO.EmotionDiaryDTO> getEmotionDiaryList(Long userId) {

        // 유저 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }

        List<EmotionDiary> diaries = emotionDiaryRepository.findAllByUserIdOrderByCreatedAtAsc(userId);
        return diaries.stream()
                .map(diary -> new EmotionDiaryResponseDTO.EmotionDiaryDTO(
                        diary.getId(),
                        diary.getContent(),
                        diary.getEmotion(),
                        diary.getSummary(),
                        diary.getFeedback(),
                        diary.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
