package com.synergyx.trading.service.backtestService;
import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.backtest.BacktestRequestDTO;
import com.synergyx.trading.dto.backtest.BacktestResponseDTO;
import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;
import com.synergyx.trading.model.Backtest;
import com.synergyx.trading.model.Pattern;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.User;
import com.synergyx.trading.repository.BacktestRepository;
import com.synergyx.trading.repository.PatternRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.repository.UserRepository;
import com.synergyx.trading.service.stockService.candle.StockCandleQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.synergyx.trading.service.backtestService.client.BacktestClientService;
import static com.synergyx.trading.util.NumberUtil.round;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BacktestServiceImpl implements BacktestService {
    private final PatternRepository patternRepository;
    private final BacktestRepository backtestRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final BacktestClientService backtestClientService;
    private final StockCandleQueryService stockCandleQueryService;

    // 백테스팅 실행
    @Override
    @Transactional
    public BacktestResponseDTO.BacktestExecutionDTO runBacktest(Long userId, Long patternId, Long stockId, BacktestRequestDTO request) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 패턴 조회
        Pattern pattern = patternRepository.findById(patternId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PATTERN_NOT_FOUND));

        // 종목 조회
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.STOCK_NOT_FOUND));

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        // 백테스팅 기간 최대 5년 제한
        if (startDate.isAfter(endDate) || startDate.plusYears(5).isBefore(endDate)) {
            throw new GeneralException(ErrorStatus.BACKTEST_PERIOD_EXCEEDS_LIMIT);

        }

        BacktestResponseDTO.BacktestExecutionDTO result = backtestClientService.callBacktestAPI(patternId, stockId, startDate, endDate);

        Backtest saved = backtestRepository.save(Backtest.builder()
                .user(user)
                .pattern(pattern)
                .stock(stock)
                .executedAt(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .winRate(result.getWinRate())
                .averageReturn(result.getAverageReturn())
                .matchedCount(result.getMatchedCount())
                .maxReturnDate(result.getMaxReturnDate())
                .maxReturn(result.getMaxReturn())
                .minReturnDate(result.getMinReturnDate())
                .minReturn(result.getMinReturn())
                .lastMatchedDate(result.getLastMatchedDate())
                .lastMatchedReturn(result.getLastMatchedReturn())
                .totalReturn(result.getTotalReturn())
                // null 허용
                .highlightFromDate(
                        result.getHighlightRange() != null ? result.getHighlightRange().getFromDate() : null
                )
                .highlightToDate(
                        result.getHighlightRange() != null ? result.getHighlightRange().getToDate() : null
                )
                .periodUnit(pattern.getPeriodUnit())
                .build());

        return BacktestResponseDTO.BacktestExecutionDTO.builder()
                .backtestId(saved.getId())
                .stockName(saved.getStock().getName())
                .executedAt(saved.getExecutedAt())
                .matchedCount(saved.getMatchedCount())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .winRate(round(saved.getWinRate()))
                .averageReturn(round(saved.getAverageReturn()))
                .maxReturn(round(saved.getMaxReturn()))
                .maxReturnDate(saved.getMaxReturnDate())
                .minReturn(round(saved.getMinReturn()))
                .minReturnDate(saved.getMinReturnDate())
                .totalReturn(round(saved.getTotalReturn()))
                .lastMatchedDate(saved.getLastMatchedDate())
                .lastMatchedReturn(round(saved.getLastMatchedReturn()))
                .highlightRange(
                        (saved.getHighlightFromDate() != null && saved.getHighlightToDate() != null)
                                ? BacktestResponseDTO.HighlightRangeDTO.builder()
                                .fromDate(saved.getHighlightFromDate())
                                .toDate(saved.getHighlightToDate())
                                .build()
                                : null
                )
                .periodUnit(saved.getPeriodUnit())
                .build();
    }

    // 과거 백테스팅 결과 상세 조회
    @Override
    @Transactional(readOnly = true)
    public BacktestResponseDTO.BacktestResultDetailDTO getBacktestResultDetail(Long userId, Long backtestId) {

        // 유저 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }

        // 백테스팅 조회
        Backtest backtest = backtestRepository.findByIdAndUserId(backtestId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BACKTEST_NOT_FOUND));

        return BacktestResponseDTO.BacktestResultDetailDTO.builder()
                .backtestId(backtestId)
                .stockName(backtest.getStock().getName())
                .stockImage(backtest.getStock().getImageUrl())
                .executedAt(backtest.getExecutedAt())
                .startDate(backtest.getStartDate())
                .endDate(backtest.getEndDate())
                .winRate(round(backtest.getWinRate()))
                .averageReturn(round(backtest.getAverageReturn()))
                .matchedCount(backtest.getMatchedCount())
                .maxReturnDate(backtest.getMaxReturnDate())
                .maxReturn(round(backtest.getMaxReturn()))
                .minReturnDate(backtest.getMinReturnDate())
                .minReturn(round(backtest.getMinReturn()))
                .lastMatchedDate(backtest.getLastMatchedDate())
                .lastMatchedReturn(round(backtest.getLastMatchedReturn()))
                .totalReturn(round(backtest.getTotalReturn()))
                .highlightRange(
                        (backtest.getHighlightFromDate() != null && backtest.getHighlightToDate() != null)
                                ? BacktestResponseDTO.HighlightRangeDTO.builder()
                                .fromDate(backtest.getHighlightFromDate())
                                .toDate(backtest.getHighlightToDate())
                                .build()
                                : null
                )
                .periodUnit(backtest.getPeriodUnit())
                .build();
    }

    // 백테스팅 결과 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<BacktestResponseDTO.BacktestSummaryDTO> getBacktestResultList(Long userId, int page, int size) {

        // 유저 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("executedAt").descending());
        Page<Backtest> resultPage = backtestRepository.findByUserId(userId, pageable);

        return resultPage.map(bt -> BacktestResponseDTO.BacktestSummaryDTO.builder()
                .backtestId(bt.getId())
                .stockName(bt.getStock().getName())
                .executedAt(bt.getExecutedAt())
                .winRate(round(bt.getWinRate()))
                .averageReturn(round(bt.getAverageReturn()))
                .matchedCount(bt.getMatchedCount())
                .build());
    }

    // 백테스팅 결과 차트 조회
    @Override
    @Transactional(readOnly = true)
    public List<StockCandleResponseDTO> getBacktestResultCandles(Long userId, Long backtestId, int margin) {

        // 유저 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }

        Backtest backtest = backtestRepository.findByIdAndUserId(backtestId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BACKTEST_NOT_FOUND));

        if (backtest.getHighlightFromDate() == null || backtest.getHighlightToDate() == null) {
            throw new GeneralException(ErrorStatus.HIGHLIGHT_RANGE_NOT_FOUND);
        }

        Long stockId = backtest.getStock().getId();
        LocalDateTime from = backtest.getHighlightFromDate();
        LocalDateTime to = backtest.getHighlightToDate();

        return switch (backtest.getPeriodUnit()) {
            case DAY -> stockCandleQueryService.getBacktestDailyCandles(
                    stockId,
                    from.toLocalDate().minusDays(margin),
                    to.toLocalDate().plusDays(margin)
            );
            case HOUR -> stockCandleQueryService.getBacktestHourlyCandles(
                    stockId,
                    from.minusHours(margin),
                    to.plusHours(margin)
            );
        };
    }
}