package com.synergyx.trading.service.stockService.ranking;

import com.synergyx.trading.dto.stockDetail.RankedStockDTO;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockDetail;
import com.synergyx.trading.model.StockOhlcv;
import com.synergyx.trading.repository.StockDetailRepository;
import com.synergyx.trading.repository.StockOhlcvRepository;
import com.synergyx.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.synergyx.trading.util.ParsingUtil.toFormattedNumber;
import static com.synergyx.trading.util.ParsingUtil.toFormattedPercentage;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockRankingQueryServiceImpl implements StockRankingQueryService {

    private final StockOhlcvRepository stockOhlcvRepository;
    private final StockDetailRepository stockDetailRepository;
    private final StockRepository stockRepository;

    /**
     * 거래대금(= 종가 * 거래량)을 기준으로 최근 데이터 기준 상위 20개 종목을 조회합니다.
     *
     * @return 거래대금 기준 TOP 20 종목 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<RankedStockDTO> getTop20() {
//        log.info("[StockRanking] 거래대금 기준 TOP 20 조회 시작");

        // 최신 timestamp 조회
        LocalDateTime latestTimestamp = stockOhlcvRepository.findLatestTimestamp();
        if (latestTimestamp == null) {
            log.warn("[StockRanking] 최신 timestamp 조회 실패");
            return Collections.emptyList();
        }

        // 거래대금 상위 20 종목 조회
        List<StockOhlcv> topOhlcvs = stockOhlcvRepository.findTopByTimestamp(
                latestTimestamp,
                PageRequest.of(0, 20)
        );
        if (topOhlcvs.isEmpty()) {
            log.warn("[StockRanking] 거래대금 기준 종목이 없습니다.");
            return Collections.emptyList();
        }

        // 종목 상세 데이터 조회 (등락률용)
        Map<Long, StockDetail> detailMap = stockDetailRepository.findAll().stream()
                .collect(Collectors.toMap(
                        sd -> sd.getStock().getId(),
                        sd -> sd,
                        (a, b) -> a // 중복 발생 시 첫 번째 값 유지
                ));

        // 랭킹 매핑
        AtomicInteger rankCounter = new AtomicInteger(1);

        List<RankedStockDTO> rankedList = topOhlcvs.stream()
                .map(ohlcv -> {
                    Stock stock = ohlcv.getStock();
                    StockDetail detail = detailMap.get(stock.getId());

                    String formattedPrice = toFormattedNumber(ohlcv.getClose());
                    String formattedChangeRate = (detail != null)
                            ? toFormattedPercentage(detail.getChangeRate(), 2)
                            : "N/A";

                    return RankedStockDTO.builder()
                            .rank(rankCounter.getAndIncrement())
                            .stockId(stock.getId())
                            .stockName(stock.getName())
                            .price(formattedPrice)
                            .changeRate(formattedChangeRate)
                            .imageUrl(stock.getImageUrl())
                            .build();
                })
                .toList();

//        log.info("[StockRanking] 거래대금 TOP 20 조회 완료 - {}개 반환", rankedList.size());
        return rankedList;
    }

    /**
     * AI 예측값의 상승률을 기준으로 상위 20개 종목을 조회합니다.
     *
     * @return AI 예측값의 15일간 평균 상승률 기준 TOP 20 종목 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<RankedStockDTO> getAiTop20() {
        log.info("[StockRanking] AI 예측 기반 TOP20 랭킹 조회 시작");

        // AI 예측 랭킹 상위 20개 (평균 상승률 기준)
        List<StockDetail> topDetails = stockDetailRepository.findTop20ByOrderByAiRankAsc();
        if (topDetails.isEmpty()) {
            log.warn("[StockRanking] AI 예측 랭킹 데이터가 없습니다.");
            return Collections.emptyList();
        }

        // 필요한 종목 ID 수집 및 매핑
        List<Long> stockIds = topDetails.stream()
                .map(detail -> detail.getStock().getId())
                .toList();

        Map<Long, Stock> stockMap = stockRepository.findAllById(stockIds).stream()
                .collect(Collectors.toMap(Stock::getId, s -> s));

        // Ohlcv 종가 데이터 가져오기
        LocalDateTime latestTimestamp = stockOhlcvRepository.findLatestTimestamp();
        List<StockOhlcv> latestOhlcvs = stockOhlcvRepository.findByTimestampAndStockIdIn(latestTimestamp, stockIds);
        Map<Long, StockOhlcv> ohlcvMap = latestOhlcvs.stream()
                .collect(Collectors.toMap(ohlcv -> ohlcv.getStock().getId(), ohlcv -> ohlcv));

        // DTO 변환
        AtomicInteger rankCounter = new AtomicInteger(1);

        List<RankedStockDTO> rankedList = topDetails.stream()
                .map(detail -> {
                    Stock stock = stockMap.get(detail.getStock().getId());
                    StockOhlcv ohlcv = ohlcvMap.get(detail.getStock().getId());

                    // 현재 종가
                    String formattedPrice = (ohlcv != null)
                            ? toFormattedNumber(ohlcv.getClose())
                            : "N/A";

                    // 예측 평균 상승률
                    String formattedPredictedIncrease = toFormattedPercentage(detail.getAiAvgIncrease(), 2);

                    return RankedStockDTO.builder()
                            .rank(rankCounter.getAndIncrement())
                            .stockId(stock.getId())
                            .stockName(stock.getName())
                            .price(formattedPrice)
                            .changeRate(formattedPredictedIncrease)
                            .imageUrl(stock.getImageUrl())
                            .build();
                })
                .toList();

//        log.info("[StockRanking] AI 예측 기반 TOP20 조회 완료 - {}개 반환", rankedList.size());
        return rankedList;
    }
}
