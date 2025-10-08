package com.synergyx.trading.dto.backtest;

// 백테스팅 랭킹에 쓰이는 순위 부여용 인터페이스
public interface Rankable {
    void setRank(Integer rank);
}