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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import static com.synergyx.trading.util.NumberUtil.round;

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

        // 백테스트 ID 목록 추출
        List<Long> backtestIds = rankings.stream()
                .map(BacktestRankingDTO::getBacktestId)
                .toList();

        // 일괄 조회
        Map<Long, Backtest> backtestMap = backtestRepository.findAllById(backtestIds)
                .stream()
                .collect(Collectors.toMap(Backtest::getId, Function.identity()));

        // 점 개수 3개 이상 조건 필터링
        rankings = rankings.stream()
                .filter(r -> {
                    Backtest backtest = backtestMap.get(r.getBacktestId());
                    if (backtest == null) {
                        throw new GeneralException(ErrorStatus.BACKTEST_NOT_FOUND);
                    }
                    return backtest.getPattern().getPoints().size() >= 3;
                })
                .toList();

        // 순위 부여
        assignRanks(rankings);

        // 반올림 처리
        rankings.forEach(r -> {
            r.setWinRate(round(r.getWinRate()));
            r.setAverageReturn(round(r.getAverageReturn()));
            r.setMaxReturn(round(r.getMaxReturn()));
        });

        return rankings;
    }
}