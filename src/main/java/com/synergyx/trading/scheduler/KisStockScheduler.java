package com.synergyx.trading.scheduler;

import com.synergyx.trading.service.kisService.KisCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KisStockScheduler {

    private final KisCommandService kisService;

    // 1분마다 실행 (초 분 시)
    @Scheduled(cron = "0 */1 * * * *")
    public void fetchRealTimeStock() {
        kisService.fetchAndSaveKospi100();  // 매 1분마다 KOSPI100 현재가 수집
    }
}
