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

        double sumErrorPct = 0.0;   // MAPE 계산용
        double sumSquaredError = 0.0; // RMSE 계산용
        double sumActual = 0.0;     // NRMSE 보정용 (평균 실제값)
        int count = 0;

        for (Object[] row : rows) {
            Long stockId = ((Number) row[0]).longValue();
            String stockCode = (String) row[1];
            LocalDate targetDate = ((Date) row[2]).toLocalDate();
            Double predicted = ((Number) row[3]).doubleValue();
            Double actual = ((Number) row[4]).doubleValue();
            Double errorPct = ((Number) row[5]).doubleValue();

            // DTO 생성
            PredictionErrorDTO dto = new PredictionErrorDTO(
                    stockId, stockCode, targetDate, predicted, actual, errorPct
            );
            errors.add(dto);

            // 오차 계산
            if (actual != null && actual != 0) {
                double diff = predicted - actual;
                sumErrorPct += errorPct;
                sumSquaredError += diff * diff;
                sumActual += actual;
                count++;
            }
        }

        // 지표 계산
        double avgErrorPct = (count > 0) ? sumErrorPct / count : 0.0; // MAPE (%)
        double rmse = (count > 0) ? Math.sqrt(sumSquaredError / count) : 0.0;
        double meanActual = (count > 0) ? (sumActual / count) : 0.0;
        double nrmse = (meanActual != 0) ? (rmse / meanActual * 100.0) : 0.0; // NRMSE (%)

        // 결과 맵 구성
        Map<String, Object> result = new HashMap<>();
        result.put("errors", errors);
        result.put("mape", avgErrorPct); // 평균 절대 오차율 (MAPE)
        result.put("rmse", rmse);               // RMSE (단위: 원)
        result.put("nrmsePct", nrmse);          // 정규화 RMSE (단위 무관, %)
        return result;
    }
}
