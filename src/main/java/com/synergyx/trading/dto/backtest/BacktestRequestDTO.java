package com.synergyx.trading.dto.backtest;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 백테스팅 실행 날짜 조건
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestRequestDTO {
    // JSON 문자열 -> LocalDate 타입 형변환
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate; // 백테스트 시작일

    // JSON 문자열 -> LocalDate 타입 형변환
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate; // 백테스트 종료일
}
