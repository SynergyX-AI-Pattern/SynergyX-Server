package com.synergyx.trading.service.predictionService;

import com.synergyx.trading.dto.stockDetail.PredictionWindowSummaryDTO;

import java.time.LocalDate;

public interface PredictionQueryService {
    PredictionWindowSummaryDTO getWindowSummary(Long stockId, LocalDate asOfDate);
}