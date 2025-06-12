package com.synergyx.trading.service.backtestService;
import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.backtest.BacktestRequestDTO;
import com.synergyx.trading.dto.backtest.BacktestResponseDTO;
import com.synergyx.trading.model.Backtest;
import com.synergyx.trading.model.Pattern;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.User;
import com.synergyx.trading.repository.BacktestRepository;
import com.synergyx.trading.repository.PatternRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BacktestServiceImpl implements BacktestService {
    private final PatternRepository patternRepository;
    private final BacktestRepository backtestRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;

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

        // fastAPI 연결 후 수정.
        // 분석 결과를 DB에 저장
        // 백테스트 분석 결과 (목데이터)
        LocalDate executedAt = LocalDate.now();
        double winRate = 65.2;
        double averageReturn = 12.5;
        int matchedCount = 8;
        LocalDate maxReturnDate = LocalDate.of(2024, 3, 15);
        double maxReturn = 25.4;
        LocalDate minReturnDate = LocalDate.of(2024, 2, 10);
        double minReturn = -7.8;
        LocalDate lastMatchedDate = LocalDate.of(2024, 4, 20);
        double lastMatchedReturn = 9.3;
        double totalReturn = 88.3;

        // DB 저장용 엔티티 생성
        Backtest backtest = Backtest.builder()
                .user(user)
                .pattern(pattern)
                .stock(stock)
                .executedAt(executedAt)
                .startDate(startDate)
                .endDate(endDate)
                .winRate(winRate)
                .averageReturn(averageReturn)
                .matchedCount(matchedCount)
                .maxReturnDate(maxReturnDate)
                .maxReturn(maxReturn)
                .minReturnDate(minReturnDate)
                .minReturn(minReturn)
                .lastMatchedDate(lastMatchedDate)
                .lastMatchedReturn(lastMatchedReturn)
                .totalReturn(totalReturn)
                .build();

        Backtest saved = backtestRepository.save(backtest);

        return BacktestResponseDTO.BacktestExecutionDTO.builder()
                .backtestId(saved.getId())
                .stockName(stock.getName())
                .executedAt(executedAt)
                .startDate(startDate)
                .endDate(endDate)
                .winRate(winRate)
                .averageReturn(averageReturn)
                .matchedCount(matchedCount)
                .maxReturnDate(maxReturnDate)
                .maxReturn(maxReturn)
                .minReturnDate(minReturnDate)
                .minReturn(minReturn)
                .lastMatchedDate(lastMatchedDate)
                .lastMatchedReturn(lastMatchedReturn)
                .totalReturn(totalReturn)
                .build();
    }
    // 과거 백테스팅 결과 상세 조회
    @Override
    @Transactional(readOnly = true)
    public BacktestResponseDTO.BacktestResultDetailDTO getBacktestResultDetail(Long userId, Long backtestId) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 백테스팅 조회
        Backtest backtest = backtestRepository.findById(backtestId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BACKTEST_NOT_FOUND));

        return BacktestResponseDTO.BacktestResultDetailDTO.builder()
                .backtestId(backtestId)
                .stockName(backtest.getStock().getName())
                .stockImage(backtest.getStock().getImageUrl())
                .executedAt(backtest.getExecutedAt())
                .startDate(backtest.getStartDate())
                .endDate(backtest.getEndDate())
                .winRate(backtest.getWinRate())
                .averageReturn(backtest.getAverageReturn())
                .matchedCount(backtest.getMatchedCount())
                .maxReturnDate(backtest.getMaxReturnDate())
                .maxReturn(backtest.getMaxReturn())
                .minReturnDate(backtest.getMinReturnDate())
                .minReturn(backtest.getMinReturn())
                .lastMatchedDate(backtest.getLastMatchedDate())
                .lastMatchedReturn(backtest.getLastMatchedReturn())
                .totalReturn(backtest.getTotalReturn())
                .build();
    }

    // 백테스팅 결과 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<BacktestResponseDTO.BacktestSummaryDTO> getBacktestResultList(Long userId, int page, int size) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by("executedAt").descending());
        Page<Backtest> resultPage = backtestRepository.findByUserId(userId, pageable);

        return resultPage.map(bt -> BacktestResponseDTO.BacktestSummaryDTO.builder()
                .backtestId(bt.getId())
                .stockName(bt.getStock().getName())
                .executedAt(bt.getExecutedAt())
                .winRate(bt.getWinRate())
                .averageReturn(bt.getAverageReturn())
                .matchedCount(bt.getMatchedCount())
                .build());
    }

}
