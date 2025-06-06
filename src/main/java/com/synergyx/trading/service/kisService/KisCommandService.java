package com.synergyx.trading.service.kisService;

public interface KisCommandService {
    void updateStockDetailsFromKis();

    void updateRoeFromKis();

    void updateRoeFromKisTest(String stockCode);
}
