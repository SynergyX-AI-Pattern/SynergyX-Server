package com.synergyx.trading.model.common;

import java.time.LocalDateTime;

public interface Ohlcv {
    LocalDateTime getTimestamp();

    Double getOpen();

    Double getClose();

    Double getHigh();

    Double getLow();

    Long getVolume();
}