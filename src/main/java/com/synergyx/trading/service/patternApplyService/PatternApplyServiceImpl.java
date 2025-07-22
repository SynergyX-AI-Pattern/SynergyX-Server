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

        LocalDateTime entryAt = request.getEntryAt() != null
               ? request.getEntryAt()
                : LocalDateTime.now();  // 감지 시작일이 null이면 현재 시간으로 대체

        // 감지 시작일이 미래일 경우 예외 처리
        if (entryAt.isAfter(LocalDateTime.now())) {
            throw new GeneralException(ErrorStatus.INVALID_ENTRY_AT);
        }

        // 매수 가격 조회
        StockOhlcv latestOhlcv = stockOhlcvRepository
                .findTop1ByStockIdAndTimestampLessThanEqualOrderByTimestampDesc(request.getStockId(), entryAt)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CANDLE_DATA_NOT_FOUND));

        Double entryPrice = latestOhlcv.getClose();

        PatternApply apply = PatternApply.builder()
                .pattern(pattern)
                .stock(stock)
                .user(user)
                .isAlertEnabled(false)
                .entryAt(entryAt)
                .entryPrice(entryPrice)
                .build();

        PatternApply saved = patternApplyRepository.save(apply);

        return PatternApplyResponseDTO.PatternApplyResultDTO.builder()
                .patternApplyId(saved.getId())
                .isAlertEnabled(saved.getIsAlertEnabled())
                .entryAt(saved.getEntryAt())
                .entryPrice(saved.getEntryPrice())
                .build();
    }

    // 패턴 알림 토글
    @Override
    @Transactional
    public PatternApplyResponseDTO.PatternApplyToggleDTO toggleNotification(Long userId, Long patternApplyId) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

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

    // 패턴 적용 정보 수정
    @Override
    @Transactional
    public void updatePatternApply(Long userId, Long patternApplyId, PatternApplyRequestDTO.PatternApplyUpdateDTO request) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 패턴 적용 정보 조회
        PatternApply patternApply = patternApplyRepository.findById(patternApplyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PATTERN_APPLY_NOT_FOUND));

        // 감지 시작일 유효성 검증
        LocalDateTime newEntryAt = request.getEntryAt();
        if (newEntryAt != null) {
            // 감지 시작일이 미래일 경우 예외 처리
            if (request.getEntryAt().isAfter(LocalDateTime.now())) {
                throw new GeneralException(ErrorStatus.INVALID_ENTRY_AT);
            }
            patternApply.setEntryAt(request.getEntryAt());

            // entryAt 변경 시 해당 시점의 가격도 업데이트
            StockOhlcv latestOhlcv = stockOhlcvRepository
                    .findTop1ByStockIdAndTimestampLessThanEqualOrderByTimestampDesc(patternApply.getStock().getId(), newEntryAt)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.CANDLE_DATA_NOT_FOUND));

            patternApply.setEntryPrice(latestOhlcv.getClose());
        }
        patternApplyRepository.save(patternApply);
    }

    // 패턴 적용 해제
    @Override
    @Transactional
    public void deletePatternApply(Long userId, Long patternApplyId) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 패턴 적용 정보 조회
        PatternApply patternApply = patternApplyRepository.findById(patternApplyId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PATTERN_APPLY_NOT_FOUND));

        patternApplyRepository.delete(patternApply);
    }

}