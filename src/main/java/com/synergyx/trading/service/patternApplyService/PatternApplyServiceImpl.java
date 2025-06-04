package com.synergyx.trading.service.patternApplyService;

import com.synergyx.trading.dto.patternApply.PatternApplyRequestDTO;
import com.synergyx.trading.dto.patternApply.PatternApplyResponseDTO;
import com.synergyx.trading.model.PatternApplyEntity;
import com.synergyx.trading.model.PatternEntity;
import com.synergyx.trading.repository.PatternApplyRepository;
import com.synergyx.trading.repository.PatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PatternApplyServiceImpl implements PatternApplyService {

    private final PatternRepository patternRepository;
    private final PatternApplyRepository patternApplyRepository;

    // 패턴 적용
    @Override
    public PatternApplyResponseDTO.PatternApplyResultDTO applyPattern(PatternApplyRequestDTO.PatternApplyDTO request) {
        PatternEntity pattern = patternRepository.findById(request.getPatternId())
                .orElseThrow(() -> new NoSuchElementException("패턴이 존재하지 않습니다."));

        PatternApplyEntity apply = PatternApplyEntity.builder()
                .pattern(pattern)
//                .stockId(request.getStockId())
                .stockId("20") // 종목 엔티티 생성 전 하드코딩
                .isAlertEnabled(false)
                .build();

        PatternApplyEntity saved = patternApplyRepository.save(apply);

        return PatternApplyResponseDTO.PatternApplyResultDTO.builder()
                .patternApplyId(saved.getId())
                .isAlertEnabled(saved.getIsAlertEnabled())
                .build();
    }

    // 패턴 알림 토글
    @Override
    public PatternApplyResponseDTO.PatternApplyResultDTO toggleNotification(Long patternApplyId) {
        PatternApplyEntity entity = patternApplyRepository.findById(patternApplyId)
                .orElseThrow(() -> new NoSuchElementException("해당 패턴 적용 정보가 존재하지 않습니다."));

        entity.setIsAlertEnabled(!entity.getIsAlertEnabled());
        PatternApplyEntity updated = patternApplyRepository.save(entity);

        return PatternApplyResponseDTO.PatternApplyResultDTO.builder()
                .patternApplyId(updated.getId())
                .isAlertEnabled(updated.getIsAlertEnabled())
                .build();
    }
}