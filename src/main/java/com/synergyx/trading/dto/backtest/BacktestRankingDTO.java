package com.synergyx.trading.dto.backtest;

import lombok.*;

import java.time.LocalDate;

// 백테스팅 랭킹 조회
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestRankingDTO implements Rankable {
    private Integer rank; // 랭킹 순위
    private Long userId; // 사용자 아이디
    private String username; // 사용자 이름
    private String image; // 사용자 이미지
    private Double winRate; // 승률
    private Double averageReturn; // 평균 수익률
    private Double maxReturn; // 최대 수익률
    private LocalDate maxReturnDate; // 최대 수익률 발생일
    private Long backtestId; // 백테스팅 아이디

    public BacktestRankingDTO(Long userId, String username, String image, Double winRate, Double averageReturn, Double maxReturn, LocalDate maxReturnDate, Long backtestId) {
        this.userId = userId;
        this.username = username;
        this.image = image;
        this.winRate = winRate;
        this.averageReturn = averageReturn;
        this.maxReturn = maxReturn;
        this.maxReturnDate = maxReturnDate;
        this.backtestId = backtestId;
    }
}
