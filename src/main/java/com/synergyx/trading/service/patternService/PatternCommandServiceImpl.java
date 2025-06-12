package com.synergyx.trading.service.patternService;
import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.dto.pattern.PatternRequestDTO;
import com.synergyx.trading.dto.pattern.PatternResponseDTO;
import com.synergyx.trading.model.Pattern;
import com.synergyx.trading.model.PeriodUnit;
import com.synergyx.trading.model.User;
import com.synergyx.trading.repository.BacktestRepository;
import com.synergyx.trading.repository.PatternApplyRepository;
import com.synergyx.trading.repository.PatternRepository;
import com.synergyx.trading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatternCommandServiceImpl implements PatternCommandService {

    private final PatternRepository patternRepository;
    private final BacktestRepository backtestRepository;
    private final PatternApplyRepository patternApplyRepository;
    private final UserRepository userRepository;

    // 패턴 생성
    @Override
    @Transactional
    public PatternResponseDTO.PatternDTO createPattern(Long userId, PatternRequestDTO dto) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        Pattern pattern = Pattern.builder()
                .patternName(dto.getPatternName())
                .points(dto.getPoints())
                .tolerance(dto.getTolerance())
                .periodValue(dto.getPeriodValue())
                .periodUnit(PeriodUnit.valueOf(dto.getPeriodUnit().toUpperCase()))
                .user(user)
                .build();

        Pattern savedPattern = patternRepository.save(pattern);

        return PatternResponseDTO.PatternDTO.builder()
                .patternId(savedPattern.getId())
                .patternName(savedPattern.getPatternName())
                .points(dto.getPoints())
                .build();
    }

    // 패턴 수정
    @Override
    @Transactional
    public void updatePattern(Long userId, Long patternId, PatternRequestDTO dto) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));


        // 패턴 조회
        Pattern pattern = patternRepository.findById(patternId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PATTERN_NOT_FOUND));

        pattern.setPatternName(dto.getPatternName());
        pattern.setPoints(dto.getPoints());
        pattern.setTolerance(dto.getTolerance());
        pattern.setPeriodValue(dto.getPeriodValue());
        pattern.setPeriodUnit(PeriodUnit.valueOf(dto.getPeriodUnit().toUpperCase()));

        patternRepository.save(pattern);
    }

    // 패턴 삭제
    @Override
    @Transactional
    public void deletePattern(Long userId, Long patternId) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        // 패턴 조회
        Pattern pattern = patternRepository.findById(patternId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PATTERN_NOT_FOUND));

        backtestRepository.deleteAllByPattern(pattern);
        patternApplyRepository.deleteAllByPattern(pattern);

        patternRepository.delete(pattern);
    }

}
