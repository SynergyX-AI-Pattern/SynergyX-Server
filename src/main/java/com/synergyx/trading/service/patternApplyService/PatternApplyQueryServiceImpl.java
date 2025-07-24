package com.synergyx.trading.service.patternApplyService;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.patternApply.PatternApplyResponseDTO;
import com.synergyx.trading.model.*;
import com.synergyx.trading.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatternApplyQueryServiceImpl implements PatternApplyQueryService {

    private final PatternRepository patternRepository;
    private final PatternApplyRepository patternApplyRepository;
    private final UserRepository userRepository;
    private final BacktestRepository backtestRepository;
    private final StockRepository stockRepository;

    // 종목-패턴 상세 조회
    @Override
    @Transactional(readOnly = true)
    public PatternApplyResponseDTO.PatternApplyDetailDTO getPatternApplyDetail(Long userId, Long stockId) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 패턴 적용 정보 조회
        PatternApply patternApply = patternApplyRepository.findByUserIdAndStockId(userId, stockId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PATTERN_APPLY_NOT_FOUND));

        // 종목 조회
        Stock stock = patternApply.getStock();

        // 패턴 조회
        Pattern pattern = patternApply.getPattern();

        // 최근 백테스트 1건
        Backtest backtest = backtestRepository
                .findTop1ByPatternIdAndStockIdAndUserIdOrderByExecutedAtDesc(
                        pattern.getId(), stock.getId(), user.getId()
                ).orElse(null);

        return PatternApplyResponseDTO.PatternApplyDetailDTO.builder()
                .patternApplyId(patternApply.getId())
                .stock(PatternApplyResponseDTO.PatternApplyDetailDTO.StockDTO.builder()
                        .stockId(stockId)
                        .stockName(stock.getName())
                        .stockImage(stock.getImageUrl())
                        .build())
                .pattern(PatternApplyResponseDTO.PatternApplyDetailDTO.PatternDTO.builder()
                        .patternId(pattern.getId())
                        .points(pattern.getPoints())
                        .tolerance(pattern.getTolerance())
                        .periodValue(pattern.getPeriodValue())
                        .periodUnit(String.valueOf(pattern.getPeriodUnit()))
                        .build())
                .backtestResult(backtest != null ? PatternApplyResponseDTO.PatternApplyDetailDTO.BacktestResultDTO.builder()
                        .backtestId(backtest.getId())
                        .executedAt(backtest.getExecutedAt())
                        .startDate(backtest.getStartDate())
                        .endDate(backtest.getEndDate())
                        .matchedCount(backtest.getMatchedCount())
                        .winRate(backtest.getWinRate())
                        .averageReturn(backtest.getAverageReturn())
                        .maxReturn(backtest.getMaxReturn())
                        .maxReturnDate(backtest.getMaxReturnDate())
                        .build() : null)
                .build();
    }
}