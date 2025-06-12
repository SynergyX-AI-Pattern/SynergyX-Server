package com.synergyx.trading.service.patternService;
import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.pattern.PatternResponseDTO;
import com.synergyx.trading.model.*;
import com.synergyx.trading.repository.BacktestRepository;
import com.synergyx.trading.repository.PatternApplyRepository;
import com.synergyx.trading.repository.PatternRepository;
import com.synergyx.trading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatternQueryServiceImpl implements PatternQueryService {

    private final PatternRepository patternRepository;
    private final PatternApplyRepository patternApplyRepository;
    private final BacktestRepository backtestRepository;
    private final UserRepository userRepository;

    // 패턴 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<PatternResponseDTO.PatternDTO> getPatternList(Long userId) {

        // 유저 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        return patternRepository.findByUserId(userId).stream()
                .map(p -> new PatternResponseDTO.PatternDTO(
                        p.getId(),
                        p.getPatternName(),
                        p.getPoints()
                ))
                .collect(Collectors.toList());
    }

    // 패턴 상세 조회
    @Override
    @Transactional(readOnly = true)
    public PatternResponseDTO.PatternDetailDTO getPatternDetail(Long userId, Long patternId) {

        // 유저 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 패턴 존재 여부 확인
        Pattern pattern = patternRepository.findById(patternId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PATTERN_NOT_FOUND));

        // 백테스트 최근 결과
        Optional<Backtest> backtest = backtestRepository.findTopByUserIdAndPatternIdOrderByExecutedAtDesc(userId, patternId);

        PatternResponseDTO.BacktestResultDTO backtestResult = backtest.map(bt -> PatternResponseDTO.BacktestResultDTO.builder()
                .backtestId(bt.getId())
                .symbol(bt.getStock().getSymbol())
                .stockName(bt.getStock().getName())
                .stockImage(bt.getStock().getImageUrl())
                .executedAt(bt.getExecutedAt())
                .startDate(bt.getStartDate())
                .endDate(bt.getEndDate())
                .winRate(bt.getWinRate())
                .averageReturn(bt.getAverageReturn())
                .matchedCount(bt.getMatchedCount())
                .maxReturn(bt.getMaxReturn())
                .maxReturnDate(bt.getMaxReturnDate())
                .build()
        ).orElse(null);

        // 패턴 적용 종목 리스트
        List<PatternApply> applyList = patternApplyRepository.findByUserIdAndPatternId(userId, patternId);

        List<PatternResponseDTO.AppliedStockDTO> appliedStocks = applyList.stream()
                .map(apply -> PatternResponseDTO.AppliedStockDTO.builder()
                        .symbol(apply.getStock().getSymbol())
                        .stockName(apply.getStock().getName())
                        .stockImage(apply.getStock().getImageUrl())
                        .build())
                .toList();

        // 최종 패턴 상세 DTO
        return PatternResponseDTO.PatternDetailDTO.builder()
                .patternId(pattern.getId())
                .patternName(pattern.getPatternName())
                .points(pattern.getPoints())
                .tolerance(pattern.getTolerance())
                .periodValue(pattern.getPeriodValue())
                .periodUnit(pattern.getPeriodUnit().name())
                .backtestResult(backtestResult)
                .appliedStockList(appliedStocks)
                .build();
    }
}
