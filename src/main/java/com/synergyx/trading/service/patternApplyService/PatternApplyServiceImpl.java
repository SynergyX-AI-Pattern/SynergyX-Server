package com.synergyx.trading.service.patternApplyService;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.patternApply.PatternApplyRequestDTO;
import com.synergyx.trading.dto.patternApply.PatternApplyResponseDTO;
import com.synergyx.trading.model.*;
import com.synergyx.trading.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PatternApplyServiceImpl implements PatternApplyService {

    private final PatternRepository patternRepository;
    private final PatternApplyRepository patternApplyRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockOhlcvRepository stockOhlcvRepository;

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

        LocalDateTime entryDate = request.getEntryDate() != null
               ? request.getEntryDate()
                : LocalDateTime.now();  // 감지 시작일이 null이면 현재 시간으로 대체

        // 매수 가격 조회
        StockOhlcv latestOhlcv = stockOhlcvRepository
                .findTop1ByStockIdAndTimestampLessThanEqualOrderByTimestampDesc(request.getStockId(), entryDate)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CANDLE_DATA_NOT_FOUND));

        Double entryPrice = latestOhlcv.getClose();

        PatternApply apply = PatternApply.builder()
                .pattern(pattern)
                .stock(stock)
                .user(user)
                .isAlertEnabled(false)
                .entryDate(entryDate)
                .entryPrice(entryPrice)
                .build();

        PatternApply saved = patternApplyRepository.save(apply);

        return PatternApplyResponseDTO.PatternApplyResultDTO.builder()
                .patternApplyId(saved.getId())
                .isAlertEnabled(saved.getIsAlertEnabled())
                .entryDate(saved.getEntryDate())
                .entryPrice(saved.getEntryPrice())
                .build();
    }

    // 패턴 알림 토글
    @Override
    @Transactional
    public PatternApplyResponseDTO.PatternApplyToggleDTO toggleNotification(Long userId, Long patternApplyId) {

        // 사용자 조회
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 패턴 적용 정보 조회
        PatternApply patternApply = patternApplyRepository.findById(patternApplyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PATTERN_APPLY_NOT_FOUND));

        patternApply.setIsAlertEnabled(!patternApply.getIsAlertEnabled());
        PatternApply updated = patternApplyRepository.save(patternApply);

        return PatternApplyResponseDTO.PatternApplyToggleDTO.builder()
                .patternApplyId(updated.getId())
                .isAlertEnabled(updated.getIsAlertEnabled())
                .build();
    }
}