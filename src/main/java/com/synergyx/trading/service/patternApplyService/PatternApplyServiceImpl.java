package com.synergyx.trading.service.patternApplyService;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.patternApply.PatternApplyRequestDTO;
import com.synergyx.trading.dto.patternApply.PatternApplyResponseDTO;
import com.synergyx.trading.model.Pattern;
import com.synergyx.trading.model.PatternApply;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.User;
import com.synergyx.trading.repository.PatternApplyRepository;
import com.synergyx.trading.repository.PatternRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatternApplyServiceImpl implements PatternApplyService {

    private final PatternRepository patternRepository;
    private final PatternApplyRepository patternApplyRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    // 패턴 적용
    @Override
    @Transactional
    public PatternApplyResponseDTO.PatternApplyResultDTO applyPattern(Long userId, PatternApplyRequestDTO.PatternApplyDTO request) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 패턴 조회
        Pattern pattern = patternRepository.findById(request.getPatternId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.PATTERN_NOT_FOUND));

        // 종목 조회
        Stock stock = stockRepository.findById(request.getStockId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.STOCK_NOT_FOUND));

        PatternApply apply = PatternApply.builder()
                .pattern(pattern)
                .stock(stock)
                .user(user)
                .isAlertEnabled(false)
                .build();

        PatternApply saved = patternApplyRepository.save(apply);

        return PatternApplyResponseDTO.PatternApplyResultDTO.builder()
                .patternApplyId(saved.getId())
                .isAlertEnabled(saved.getIsAlertEnabled())
                .build();
    }

    // 패턴 알림 토글
    @Override
    @Transactional
    public PatternApplyResponseDTO.PatternApplyResultDTO toggleNotification(Long userId, Long patternApplyId) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 패턴 적용 정보 조회
        PatternApply patternApply = patternApplyRepository.findById(patternApplyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PATTERN_APPLY_NOT_FOUND));

        patternApply.setIsAlertEnabled(!patternApply.getIsAlertEnabled());
        PatternApply updated = patternApplyRepository.save(patternApply);

        return PatternApplyResponseDTO.PatternApplyResultDTO.builder()
                .patternApplyId(updated.getId())
                .isAlertEnabled(updated.getIsAlertEnabled())
                .build();
    }
}