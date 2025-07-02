package com.synergyx.trading.service.stockService.detail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synergyx.trading.dto.stockDetail.StockDetailResponseDTO;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockDetail;
import com.synergyx.trading.repository.InterestStockRepository;
import com.synergyx.trading.repository.StockDetailRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.service.InterestStockService.InterestStockCommandService;
import com.synergyx.trading.service.kisService.KisDividendScheduleService;
import com.synergyx.trading.service.predictionService.PredictionQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.synergyx.trading.util.ParsingUtil.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockDetailQueryServiceImpl implements StockDetailQueryService {

    private final StockRepository stockRepository;
    private final StockDetailRepository stockDetailRepository;
    private final InterestStockRepository interestStockRepository;
    private final PredictionQueryService predictionQueryService;
    private final InterestStockCommandService interestStockCommandService;
    private final KisDividendScheduleService kisDividendScheduleService;

    /**
     * 종목 상세 정보를 조회합니다.
     *
     * @param stockId
     * @param userId
     * @return StockDetailResponseDTO
     */
    @Override
    @Transactional(readOnly = true)
    public StockDetailResponseDTO getStockDetail(Long stockId, Long userId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("종목을 찾을 수 없습니다."));

        StockDetail stockDetail = stockDetailRepository.findByStock_Id(stockId)
                .orElseThrow(() -> new IllegalArgumentException("종목 상세 데이터를 찾을 수 없습니다."));

        // 최근 조회 테이블에 저장
        interestStockCommandService.addRecentView(userId, stockId);

        boolean isWatchlist = interestStockRepository.existsByUserIdAndStockId(userId, stockId);

        // todo: 아직 미구현 → false 고정
        boolean isTradeNotificationEnabled = false;

        StockDetailResponseDTO.PredictionDTO prediction = predictionQueryService.getPredictionByStockId(stock.getId());

        StockDetailResponseDTO.FinancialsDTO financials = parseFinancialData(stockDetail);

        return StockDetailResponseDTO.builder()
                .stockName(stock.getName())
                .price(toFormattedNumber(stockDetail.getPrice()))
                .changeRate(stockDetail.getChangeRate() + "%")
                .changeAmount(toFormattedNumber(stockDetail.getChangeAmount()))
                .isWatchlist(isWatchlist)
                .isTradeNotificationEnabled(isTradeNotificationEnabled)
                .prediction(prediction)
                .financials(financials)
                .build();
    }

    /**
     * 재무 데이터를 문자열 형식에 맞게 파싱합니다.
     *
     * @param stockDetail 종목 상세
     * @return FinancialsDTO
     */
    private StockDetailResponseDTO.FinancialsDTO parseFinancialData(StockDetail stockDetail) {
        try {
            JsonNode node = new ObjectMapper().readTree(stockDetail.getFinancialData());

            // 배당수익률 계산
            // 배당금 정보가 없거나 계산 불가할 경우 "-"로 대체
            Double dividendYield = kisDividendScheduleService.calculateDividendYield(stockDetail);
            String formattedDividendYield = (dividendYield == null)
                    ? "-"
                    : toFormattedPercentage(dividendYield, 2);

            return StockDetailResponseDTO.FinancialsDTO.builder()
                    .pbr(toFormattedRatio(node.get("pbr").asText()))
                    .per(toFormattedRatio(node.get("per").asText()))
                    .psr(toFormattedRatio(node.get("psr").asText()))
                    .roe(toFormattedPercentage(node.get("roe").asText(), 1))
                    .marketCap(toFormattedMarketCap(node.get("market_cap").asText()))
                    .dividendYield(formattedDividendYield)
                    .build();
        } catch (Exception e) {
            log.error("[DETAIL] 재무데이터 파싱 실패 - symbol Id: {}", stockDetail.getId(), e);
            throw new RuntimeException("재무데이터 파싱 실패", e);
        }
    }
}

