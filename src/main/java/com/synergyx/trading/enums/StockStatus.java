package com.synergyx.trading.enums;

public enum StockStatus {
    LISTED,         // KOSPI100 종목
    LISTED_OUTSIDE, // db에 없는 상장 기업
    UNLISTED,       // 비상장 브랜드/기업
    UNKNOWN         // 판단 불가
}