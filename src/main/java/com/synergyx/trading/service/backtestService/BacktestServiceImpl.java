package com.synergyx.trading.service.backtestService;
import com.synergyx.trading.dto.backtest.BacktestRequestDTO;
import com.synergyx.trading.dto.backtest.BacktestResponseDTO;
import com.synergyx.trading.model.BacktestEntity;
import com.synergyx.trading.model.PatternEntity;
import com.synergyx.trading.repository.BacktestRepository;
import com.synergyx.trading.repository.PatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BacktestServiceImpl implements BacktestService {
    private final PatternRepository patternRepository;
    private final BacktestRepository backtestRepository;

    // 백테스팅 실행
    @Override
    public BacktestResponseDTO.BacktestExecutionDTO runBacktest(Long patternId, Long stockId, BacktestRequestDTO request) {
        PatternEntity pattern = patternRepository.findById(patternId)
                .orElseThrow(() -> new IllegalArgumentException("패턴이 존재하지 않습니다."));
        // 종목 엔티티 구현 전 -> 구현 후 캡션 제거
//        StockEntity stock = stockRepository.findById(stockId)
//                .orElseThrow(() -> new IllegalArgumentException("종목이 존재하지 않습니다."));

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        // 백테스팅 기간 최대 5년 제한
        if (startDate.isAfter(endDate) || startDate.plusYears(5).isBefore(endDate)) {
            throw new IllegalArgumentException("실행 기간은 최대 5년 입니다.");
        }

        // 백테스트 분석 로직 호출
        // fastAPI 연결 후 수정.
        // 분석 결과 (목데이터)
        BacktestResponseDTO.BacktestExecutionDTO resultDto = BacktestResponseDTO.BacktestExecutionDTO.builder()
                .stockName("카카오")  // 임시 종목명 -> 실제 연동 시 stockId로 조회 예정
                .executedAt(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .winRate(65.2)
                .averageReturn(12.5)
                .matchedCount(8)
                .maxReturnDate(LocalDate.of(2024, 3, 15))
                .maxReturn(25.4)
                .minReturnDate(LocalDate.of(2024, 2, 10))
                .minReturn(-7.8)
                .lastMatchedDate(LocalDate.of(2024, 4, 20))
                .lastMatchedReturn(9.3)
                .totalReturn(88.3)
                .build();

        // fastAPI 연결 후 수정.
        // 분석 결과를 DB에 저장
        BacktestEntity entity = BacktestEntity.builder()
                .pattern(pattern)
                .stockId(stockId)
                .executedAt(resultDto.getExecutedAt())
                .startDate(resultDto.getStartDate())
                .endDate(resultDto.getEndDate())
                .winRate(resultDto.getWinRate())
                .averageReturn(resultDto.getAverageReturn())
                .matchedCount(resultDto.getMatchedCount())
                .maxReturnDate(resultDto.getMaxReturnDate())
                .maxReturn(resultDto.getMaxReturn())
                .minReturnDate(resultDto.getMinReturnDate())
                .minReturn(resultDto.getMinReturn())
                .lastMatchedDate(resultDto.getLastMatchedDate())
                .lastMatchedReturn(resultDto.getLastMatchedReturn())
                .totalReturn(resultDto.getTotalReturn())
                .build();

        backtestRepository.save(entity);

        return resultDto;
    }

    // 과거 백테스팅 결과 상세 조회
    @Override
    public BacktestResponseDTO.BacktestResultDetailDTO getBacktestResultDetail(Long backtestId) {
        BacktestEntity entity = backtestRepository.findById(backtestId)
                .orElseThrow(() -> new NoSuchElementException("백테스팅 결과가 존재하지 않습니다."));

        return BacktestResponseDTO.BacktestResultDetailDTO.builder()
                .stockName("삼성전자") // 종목 엔티티 생성 전 하드코딩
//                .stockName(entity.getStock().getStockName) // 종목 엔티티 생성 후 캡션 제거
                .stockImage("imageurl") // 종목 엔티티 생성 전 하드코딩
//                .stockImage(bt.getStock().getImageUrl()) // 종목 엔티티 생성 후 캡션 제거
                .executedAt(entity.getExecutedAt())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .winRate(entity.getWinRate())
                .averageReturn(entity.getAverageReturn())
                .matchedCount(entity.getMatchedCount())
                .maxReturnDate(entity.getMaxReturnDate())
                .maxReturn(entity.getMaxReturn())
                .minReturnDate(entity.getMinReturnDate())
                .minReturn(entity.getMinReturn())
                .lastMatchedDate(entity.getLastMatchedDate())
                .lastMatchedReturn(entity.getLastMatchedReturn())
                .totalReturn(entity.getTotalReturn())
                .build();
    }

    // 백테스팅 결과 목록 조회
    @Override
    public Page<BacktestResponseDTO.BacktestSummaryDTO> getBacktestResultList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("executedAt").descending());
        Page<BacktestEntity> resultPage = backtestRepository.findAll(pageable);

        return resultPage.map(entity -> BacktestResponseDTO.BacktestSummaryDTO.builder()
                .backtestId(entity.getId())
                .stockName("삼성전자") // 종목 엔티티 생성 전 하드코딩
//                .stockName(entity.getStock().getStockName) // 종목 엔티티 생성 후 캡션 제거
                .executedAt(entity.getExecutedAt())
                .winRate(entity.getWinRate())
                .averageReturn(entity.getAverageReturn())
                .matchedCount(entity.getMatchedCount())
                .build());
    }

}
