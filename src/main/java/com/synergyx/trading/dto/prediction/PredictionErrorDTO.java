package com.synergyx.trading.dto.prediction;

import java.time.LocalDate;

public record PredictionErrorDTO(
        Long stockId,
        String stockCode,
        LocalDate targetDate,
        Double predictedClose,
        Double actualClose,
        Double errorPct
) {}