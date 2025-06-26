package com.synergyx.trading.service.stockService.detail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synergyx.trading.dto.stockDetail.StockDetailResponseDTO;
import com.synergyx.trading.model.Stock;
import com.synergyx.trading.model.StockDetail;
import com.synergyx.trading.repository.InterestStockRepository;
import com.synergyx.trading.repository.StockDetailRepository;
import com.synergyx.trading.repository.StockRepository;
import com.synergyx.trading.service.predictionService.PredictionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.synergyx.trading.util.ParsingUtil.*;

@Service
@RequiredArgsConstructor
public class StockDetailQueryServiceImpl implements StockDetailQueryService {

    private final StockRepository stockRepository;
    private final StockDetailRepository stockDetailRepository;
    private final InterestStockRepository interestStockRepository;
    private final PredictionQueryService predictionQueryService;

    /**
     * 종목 상세 정보를 조회합니다.
     *
     * @param stockId
     * @param userId
     * @return StockDetailResponseDTO
     */
    @Override
    public StockDetailResponseDTO getStockDetail(Long stockId, Long userId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("종목을 찾을 수 없습니다."));

        StockDetail stockDetail = stockDetailRepository.findByStock_Id(stockId)
                .orElseThrow(() -> new IllegalArgumentException("종목 상세 데이터를 찾을 수 없습니다."));

        boolean isWatchlist = interestStockRepository.existsByUserIdAndStockId(userId, stockId);

        // todo: 아직 미구현 → false 고정
        boolean isTradeNotificationEnabled = false;

        StockDetailResponseDTO.PredictionDTO prediction = predictionQueryService.getPredictionByStockId(stock.getId());

        StockDetailResponseDTO.FinancialsDTO financials = parseFinancialData(stockDetail.getFinancialData());

        return StockDetailResponseDTO.builder()
                .stockName(stock.getName())
                .price(toFormattedNumber(stockDetail.getPrice()))
                .changeRate(stockDetail.getChangeRate() + "%")
                //Todo: change amount 추가
//                .changeAmount(toFormattedNumber(stockDetail.getChangeAmount()))
                .changeAmount("600")
                .isWatchlist(isWatchlist)
                .isTradeNotificationEnabled(isTradeNotificationEnabled)
                .prediction(prediction)
                .financials(financials)
                .build();
    }

    /**
     * 재무 데이터를 문자열 형식에 맞게 파싱합니다.
     *
     * @param json 재무 데이터
     * @return FinancialsDTO
     */
    private StockDetailResponseDTO.FinancialsDTO parseFinancialData(String json) {
        try {
            JsonNode node = new ObjectMapper().readTree(json);
            return StockDetailResponseDTO.FinancialsDTO.builder()
                    .pbr(toFormattedRatio(node.get("pbr").asText()))
                    .per(toFormattedRatio(node.get("per").asText()))
                    .psr(toFormattedRatio(node.get("psr").asText()))
                    .roe(toFormattedPercentage(node.get("roe").asText(), 1))
                    .marketCap(toFormattedMarketCap(node.get("market_cap").asText()))
                    // todo : 배당수익률 데이터 불러온 후 수정해야 함.
//                    .dividendYield(toFormattedPercentage(node.get("dividend_yield").asText(), 2))
                    .dividendYield(null)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("재무데이터 파싱 실패", e);
        }
    }
}

