package com.synergyx.trading.service.predictionService;

import com.synergyx.trading.dto.prediction.PredictionErrorDTO;
import com.synergyx.trading.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PredictionEvaluationService {

    private final PredictionRepository predictionRepository;

    public Map<String, Object> getPredictionErrors() {
        List<Object[]> rows = predictionRepository.findPredictionErrorsRaw();
        List<PredictionErrorDTO> errors = new ArrayList<>();

        double sumError = 0.0;
        int count = 0;

        for (Object[] row : rows) {
            Long stockId = ((Number) row[0]).longValue();
            String stockCode = (String) row[1];
            LocalDate targetDate = ((Date) row[2]).toLocalDate();
            Double predicted = ((Number) row[3]).doubleValue();
            Double actual = ((Number) row[4]).doubleValue();
            Double errorPct = ((Number) row[5]).doubleValue();

            PredictionErrorDTO dto = new PredictionErrorDTO(
                    stockId, stockCode, targetDate, predicted, actual, errorPct
            );
            errors.add(dto);

            sumError += errorPct;
            count++;
        }

        double avgError = (count > 0) ? sumError / count : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("errors", errors);
        result.put("avgErrorPct", avgError);
        return result;
    }
}

