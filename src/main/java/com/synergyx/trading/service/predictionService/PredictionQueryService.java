package com.synergyx.trading.service.predictionService;

import com.synergyx.trading.dto.stockDetail.StockDetailResponseDTO;

public interface PredictionQueryService {
    StockDetailResponseDTO.PredictionDTO getPredictionByStockId(Long stockId);
}