package com.synergyx.trading.service.stockService.ranking;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
     * AI 예측값의 상승폭을 기준으로 상위 20개 종목을 조회합니다.
     *
     * @return AI 예측값의 상승폭 기준 TOP 20 종목 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<RankedStockDTO> getAiTop20() {
        log.info("[StockRanking] AI 예측 기반 TOP20 랭킹 조회 시작");

        // todo: 임시 데이터
        List<Long> stockIds = IntStream.rangeClosed(1, 20)
                .mapToObj(Long::valueOf)
                .toList();

        // stock 조회
        Map<Long, Stock> stockMap = stockRepository.findAllById(stockIds).stream()
                .collect(Collectors.toMap(Stock::getId, s -> s));

        List<RankedStockDTO> result = IntStream.rangeClosed(1, 20)
                .mapToObj(rank -> {
                    long stockId = rank;
                    Stock stock = Optional.ofNullable(stockMap.get(stockId))
                            .orElseThrow(() -> new GeneralException(ErrorStatus.STOCK_NOT_FOUND));

                    return RankedStockDTO.builder()
                            .rank(rank)
                            .stockId(stockId)
                            .stockName(stock.getName())
                            .price(getMockPrice(rank)) // todo: 임시 예측 종가
                            .changeRate(getMockChangeRate(rank)) // todo: 임시 상승률
                            .imageUrl(stock.getImageUrl())
                            .build();
                }).toList();

//        log.info("[StockRanking] AI 랭킹 조회 완료 - {}개 반환", result.size());
        return result;
    }

    /**
     * 임시 목데이터 - 예측 종가
     * todo: delete
     */
    private String getMockPrice(int rank) {
        return switch (rank) {
            case 1 -> "59,100";
            case 2 -> "112,000";
            case 3 -> "74,300";
            case 4 -> "48,900";
            case 5 -> "126,000";
            case 6 -> "191,000";
            case 7 -> "91,400";
            case 8 -> "474,000";
            case 9 -> "513,000";
            case 10 -> "169,000";
            case 11 -> "28,500";
            case 12 -> "41,600";
            case 13 -> "495,000";
            case 14 -> "19,600";
            case 15 -> "144,000";
            case 16 -> "23,900";
            case 17 -> "345,000";
            case 18 -> "151,000";
            case 19 -> "85,200";
            case 20 -> "92,300";
            default -> "0";
        };
    }

    /**
     * 임시 목데이터 - 예측 상승률
     * todo: delete
     */
    private String getMockChangeRate(int rank) {
        return switch (rank) {
            case 1 -> "2.25%";
            case 2 -> "-0.82%";
            case 3 -> "1.12%";
            case 4 -> "-1.20%";
            case 5 -> "0.95%";
            case 6 -> "3.11%";
            case 7 -> "2.78%";
            case 8 -> "-0.65%";
            case 9 -> "0.85%";
            case 10 -> "1.02%";
            case 11 -> "2.00%";
            case 12 -> "-0.45%";
            case 13 -> "1.57%";
            case 14 -> "-1.90%";
            case 15 -> "0.65%";
            case 16 -> "1.33%";
            case 17 -> "0.48%";
            case 18 -> "-0.95%";
            case 19 -> "0.75%";
            case 20 -> "1.18%";
            default -> "0.00%";
        };
    }

}
