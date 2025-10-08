package com.synergyx.trading.service.backtestService.ranking;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.backtest.BacktestRankingDTO;
import com.synergyx.trading.dto.backtest.Rankable;
import com.synergyx.trading.model.Backtest;
import com.synergyx.trading.repository.BacktestRankingRepository;
import com.synergyx.trading.repository.BacktestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class BacktestRankingService {

    private final BacktestRepository backtestRepository;
    private final BacktestRankingRepository backtestRankingRepository;

    // 랭킹 순위 부여용 메서드
    private <T extends Rankable> void assignRanks(List<T> results) {
        AtomicInteger rank = new AtomicInteger(1);
        results.forEach(r -> r.setRank(rank.getAndIncrement()));
    }

    // 랭킹 조회 (최대 수익률, 월간 기준)
    public List<BacktestRankingDTO> getRanking(int limit) {

        // 월간 기간 계산 (1일 ~ 31일)
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        // Repository 호출
        List<BacktestRankingDTO> rankings = backtestRankingRepository.findMonthlyUserMaxReturnRankings(startOfMonth, endOfMonth, PageRequest.of(0, limit));

        // 점 개수 3개 이상 조건 필터링
        rankings = rankings.stream()
                .filter(r -> {
                    Backtest backtest = backtestRepository.findById(r.getBacktestId())
                            .orElseThrow(() -> new GeneralException(ErrorStatus.BACKTEST_NOT_FOUND));
                    return backtest.getPattern().getPoints().size() >= 3;
                })
                .toList();

        // 순위 부여
        assignRanks(rankings);

        return rankings;
    }
}