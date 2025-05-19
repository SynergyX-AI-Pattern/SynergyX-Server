package com.synergyx.trading.dto.pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternRequestDTO {
    private String patternName; // 패턴 이름
//    private String symbol; // 종목 코드
    private List<Double> points; // 좌표
    private Double tolerance; // 오차 범위
    private Integer periodValue; // 기간 수치
    private String periodUnit; // 기간 단위
}
