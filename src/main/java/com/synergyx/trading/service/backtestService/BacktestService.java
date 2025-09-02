package com.synergyx.trading.service.backtestService;

import com.synergyx.trading.dto.backtest.BacktestRequestDTO;
import com.synergyx.trading.dto.backtest.BacktestResponseDTO;
import com.synergyx.trading.dto.stockDetail.StockCandleResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BacktestService {
    // 백테스팅 실행
    BacktestResponseDTO.BacktestExecutionDTO runBacktest(Long userId, Long patternId, Long stockId, BacktestRequestDTO request);

    // 백테스팅 결과 상세 조회
    BacktestResponseDTO.BacktestResultDetailDTO getBacktestResultDetail(Long userId, Long backtestId);

    // 백테스팅 결과 목록 조회
    Page<BacktestResponseDTO.BacktestSummaryDTO> getBacktestResultList(Long userId, int page, int size);

    // 백테스팅 결과 차트 조회
    List<StockCandleResponseDTO> getBacktestResultCandles(Long userId, Long backtestId, int margin);
}
