package com.synergyx.trading.service.predictionService;

import com.synergyx.trading.dto.stockDetail.StockDetailResponseDTO;
import org.springframework.stereotype.Service;

import static com.synergyx.trading.util.ParsingUtil.toFormattedNumber;

@Service
public class PredictionQueryServiceImpl implements PredictionQueryService {
    @Override
    public StockDetailResponseDTO.PredictionDTO getPredictionByStockId(Long stockId) {
        // TODO: 추후 predict 테이블에서 조회로 변경
        return StockDetailResponseDTO.PredictionDTO.builder()
                .upperBound(toFormattedNumber(63000.0))
                .lowerBound(toFormattedNumber(53100.0))
                .buyPrice(toFormattedNumber(52900.0))
                .sellPrice(toFormattedNumber(62500.0))
                .targetRange("20일 이내")
                .build();
    }
}