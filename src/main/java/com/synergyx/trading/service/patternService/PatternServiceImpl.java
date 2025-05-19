package com.synergyx.trading.service.patternService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synergyx.trading.dto.pattern.PatternRequestDTO;
import com.synergyx.trading.dto.pattern.PatternResponseDTO;
import com.synergyx.trading.model.PatternEntity;
import com.synergyx.trading.model.PeriodUnit;
import com.synergyx.trading.repository.PatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatternServiceImpl implements PatternService {

    private final PatternRepository patternRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 패턴 생성 (임시 유저 아이디)
    @Override
    public PatternResponseDTO.PatternDTO createPattern(String userId, PatternRequestDTO dto) {
        String pointsJson;
        try {
            pointsJson = objectMapper.writeValueAsString(dto.getPoints());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("points 변환 실패", e);
        }

        PatternEntity patternEntity = PatternEntity.builder()
                .patternName(dto.getPatternName())
                .points(dto.getPoints())
//                .symbol(dto.getSymbol())
                .tolerance(dto.getTolerance())
                .periodValue(dto.getPeriodValue())
                .periodUnit(PeriodUnit.valueOf(dto.getPeriodUnit().toUpperCase()))
                .userId(userId) // 임시 유저 아이디
                .build();

        PatternEntity savedPattern = patternRepository.save(patternEntity);

        return PatternResponseDTO.PatternDTO.builder()
                .patternId(savedPattern.getId())
                .patternName(savedPattern.getPatternName())
                .points(dto.getPoints())
                .build();
    }

    // 패턴 목록 조회 (임시 유저 아이디)
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

    // 패턴 상세 조회 (임시 유저 아이디)
    @Override
    public PatternResponseDTO.PatternDetailDTO getPatternDetail(Long patternId, String userId) {
        PatternEntity entity = patternRepository.findById(patternId)
                .orElseThrow(() -> new NoSuchElementException("패턴이 없습니다"));

        return new PatternResponseDTO.PatternDetailDTO(
                entity.getId(),
                entity.getPatternName(),
                entity.getPoints(),
                entity.getTolerance(),
                entity.getPeriodValue(),
                entity.getPeriodUnit().name(),

                // 임시 목데이터
                new PatternResponseDTO.BacktestResultDTO(102L, "NFLX", "Netflix, Inc", "imageurl", "2024-04-25", 58.0, 9.7, 5),

                // 임시 목데이터
                Arrays.asList(
                        new PatternResponseDTO.AppliedStockDTO("NFLX", "Netflix, Inc", "imageurl"),
                        new PatternResponseDTO.AppliedStockDTO("AAPL", "Apple, Inc", "imageurl")
                )
        );
    }

    // 패턴 수정 (임시 유저 아이디)
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

    // 패턴 삭제
    @Override
    public void deletePattern(Long patternId, String userId) {
        PatternEntity entity = patternRepository.findById(patternId)
                .orElseThrow(() -> new NoSuchElementException("패턴이 존재하지 않습니다."));
        patternRepository.delete(entity);
    }

}
