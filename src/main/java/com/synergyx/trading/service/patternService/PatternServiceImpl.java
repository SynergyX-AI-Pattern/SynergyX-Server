package com.synergyx.trading.service.patternService;
import com.synergyx.trading.dto.pattern.PatternRequestDTO;
import com.synergyx.trading.dto.pattern.PatternResponseDTO;
import com.synergyx.trading.model.BacktestEntity;
import com.synergyx.trading.model.PatternApplyEntity;
import com.synergyx.trading.model.PatternEntity;
import com.synergyx.trading.model.PeriodUnit;
import com.synergyx.trading.repository.BacktestRepository;
import com.synergyx.trading.repository.PatternApplyRepository;
import com.synergyx.trading.repository.PatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatternServiceImpl implements PatternService {

    private final PatternRepository patternRepository;
    private final PatternApplyRepository patternApplyRepository;
    private final BacktestRepository backtestRepository;


    // 패턴 생성 (임시 유저 아이디) -> 유저 엔티티 생성 후 수정 예정
    @Override
    public PatternResponseDTO.PatternDTO createPattern(String userId, PatternRequestDTO dto) {
        PatternEntity patternEntity = PatternEntity.builder()
                .patternName(dto.getPatternName())
                .points(dto.getPoints())
                .tolerance(dto.getTolerance())
                .periodValue(dto.getPeriodValue())
                .periodUnit(PeriodUnit.valueOf(dto.getPeriodUnit().toUpperCase()))
                .userId(userId) // 임시 유저 아이디 -> 유저 엔티티 생성 후 수정 예정
                .build();

        PatternEntity savedPattern = patternRepository.save(patternEntity);

        return PatternResponseDTO.PatternDTO.builder()
                .patternId(savedPattern.getId())
                .patternName(savedPattern.getPatternName())
                .points(dto.getPoints())
                .build();
    }

    // 패턴 목록 조회 (임시 유저 아이디) -> 유저 엔티티 생성 후 수정 예정
    @Override
    public List<PatternResponseDTO.PatternDTO> getPatternList(String userId) {
        return patternRepository.findByUserId(userId).stream()
                .map(entity -> new PatternResponseDTO.PatternDTO(
                        entity.getId(),
                        entity.getPatternName(),
                        entity.getPoints()
                ))
                .collect(Collectors.toList());
    }

    // 패턴 상세 조회 (임시 유저 아이디) -> 유저 엔티티 생성 후 수정 예정
    @Override
    public PatternResponseDTO.PatternDetailDTO getPatternDetail(Long patternId, String userId) {
        PatternEntity entity = patternRepository.findById(patternId)
                .orElseThrow(() -> new NoSuchElementException("패턴이 존재하지 않습니다."));

        // 백테스트 최근 결과
        Optional<BacktestEntity> backtest = backtestRepository
                .findTopByPatternIdOrderByExecutedAtDesc(patternId);

        PatternResponseDTO.BacktestResultDTO backtestResult = backtest.map(bt -> PatternResponseDTO.BacktestResultDTO.builder()
                .backtestId(bt.getId())
                .symbol("NFLX") // 종목 엔티티 생성 전 하드코딩
                .stockName("Netflix, Inc") // 종목 엔티티 생성 전 하드코딩
                .stockImage("imageurl") // 종목 엔티티 생성 전 하드코딩
//                .symbol(bt.getStock().getSymbol()) // 종목 엔티티 생성 후 캡션 제거
//                .stockName(bt.getStock().getName()) // 종목 엔티티 생성 후 캡션 제거
//                .stockImage(bt.getStock().getImageUrl()) // 종목 엔티티 생성 후 캡션 제거
                .executedAt(bt.getExecutedAt())
                .startDate(bt.getStartDate())
                .endDate(bt.getEndDate())
                .winRate(bt.getWinRate())
                .averageReturn(bt.getAverageReturn())
                .matchedCount(bt.getMatchedCount())
                .maxReturn(bt.getMaxReturn())
                .maxReturnDate(bt.getMaxReturnDate())
                .build()).orElse(null);

        // 임시 목데이터 생성. -> 종목 엔티티 구현 후 코드 제거 예정
        List<PatternResponseDTO.AppliedStockDTO> appliedStocks = List.of(
                PatternResponseDTO.AppliedStockDTO.builder()
                        .symbol("NFLX")
                        .stockName("Netflix, Inc")
                        .stockImage("imageurl")
                        .build(),
                PatternResponseDTO.AppliedStockDTO.builder()
                        .symbol("AAPL")
                        .stockName("Apple, Inc")
                        .stockImage("imageurl")
                        .build()
        );

//        종목 엔티티 구현 후 캡션 제거
//        List<PatternApplyEntity> applyList = patternApplyRepository.findByPatternId(patternId);
//        List<PatternResponseDTO.AppliedStockDTO> appliedStocks = applyList.stream()
//                .map(apply -> PatternResponseDTO.AppliedStockDTO.builder()
//                        .symbol(apply.getStock().getSymbol())
//                        .stockName(apply.getStock().getName())
//                        .stockImage(apply.getStock().getImageUrl())
//                        .build())
//                .toList();

        return PatternResponseDTO.PatternDetailDTO.builder()
                .patternId(entity.getId())
                .patternName(entity.getPatternName())
                .points(entity.getPoints())
                .tolerance(entity.getTolerance())
                .periodValue(entity.getPeriodValue())
                .periodUnit(entity.getPeriodUnit().name())
                .backtestResult(backtestResult)
                .appliedStockList(appliedStocks)
                .build();
    }

    // 패턴 수정 (임시 유저 아이디) -> 유저 엔티티 생성 후 수정 예정
    @Override
    public void updatePattern(Long patternId, String userId, PatternRequestDTO dto) {
        PatternEntity entity = patternRepository.findById(patternId)
                .orElseThrow(() -> new NoSuchElementException("패턴이 존재하지 않습니다."));

        entity.setPatternName(dto.getPatternName());
        entity.setPoints(dto.getPoints());
        entity.setTolerance(dto.getTolerance());
        entity.setPeriodValue(dto.getPeriodValue());
        entity.setPeriodUnit(PeriodUnit.valueOf(dto.getPeriodUnit().toUpperCase()));
        entity.setUpdatedAt(LocalDateTime.now());

        patternRepository.save(entity);
    }

    // 패턴 삭제 (임시 유저 아이디) -> 유저 엔티티 생성 후 수정 예정
    @Override
    public void deletePattern(Long patternId, String userId) {
        PatternEntity entity = patternRepository.findById(patternId)
                .orElseThrow(() -> new NoSuchElementException("패턴이 존재하지 않습니다."));
        patternRepository.delete(entity);
    }

}
