package com.synergyx.trading.service.backtestService;

import com.synergyx.trading.dto.backtest.BacktestRequestDTO;
import com.synergyx.trading.dto.backtest.BacktestResponseDTO;
import org.springframework.data.domain.Page;

public interface BacktestService {
    // 백테스팅 실행
    BacktestResponseDTO.BacktestExecutionDTO runBacktest(Long patternId, Long stockId, BacktestRequestDTO request);

    // 백테스팅 결과 상세 조회
    BacktestResponseDTO.BacktestResultDetailDTO getBacktestResultDetail(Long backtestId);

    // 백테스팅 결과 목록 조회
    Page<BacktestResponseDTO.BacktestSummaryDTO> getBacktestResultList(int page, int size);

}
