package com.synergyx.trading.service.InterestStockService;

public interface InterestStockCommandService {
    void addInterest(Long userId, Long stockId);
    void removeInterest(Long userId, Long stockId);
}
