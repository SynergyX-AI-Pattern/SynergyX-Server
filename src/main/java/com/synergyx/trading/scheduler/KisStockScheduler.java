package com.synergyx.trading.scheduler;

import com.synergyx.trading.service.kisService.KisOhlcvUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KisStockScheduler {

    private final KisOhlcvUpdateService kisOhlcvUpdateService;

    /**
     * 매 1분마다 실행 (장 운영시간 기준: 09시~15시59분, 주말 제외)
     */
    @Scheduled(cron = "10 * 9-15 * * MON-FRI")
    public void fetchOhlcvEveryMinute() {
        log.info("[스케줄러] 1분봉 데이터 수집 시작");
        kisOhlcvUpdateService.updateOhlcvAll();
    }
}