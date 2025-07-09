package com.synergyx.trading.scheduler;

import com.synergyx.trading.service.kisService.Kis1mOhlcvUpdateService;
import com.synergyx.trading.service.kisService.KisOhlcvUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Slf4j
public class KisStockScheduler {

    // todo: 로컬에서 스케줄링 안되게 수정

    private final KisOhlcvUpdateService kisOhlcvUpdateService;
    private final Kis1mOhlcvUpdateService kis1mOhlcvUpdateService;

    /**
     * 매 1분마다 실행 (장 운영시간 기준: 09시~15시59분, 주말 제외)
     */
//    @Scheduled(cron = "10 * 9-15 * * MON-FRI", zone = "Asia/Seoul")
//    public void fetchOhlcvEveryMinute() {
//        log.info("[스케줄러] 1분봉 데이터 수집 시작");
//        kisOhlcvUpdateService.updateOhlcvAll();
//    }

    /**
     * 매 15분 10초에 실행 (장 운영시간 기준: 09시~15시59분, 주말 제외)
     */
    @Scheduled(cron = "10 */15 9-15 * * MON-FRI", zone = "Asia/Seoul")
    public void fetch15minOhlcv() {
        log.info("[스케줄러] 15분봉 수집 시작");
        kisOhlcvUpdateService.updateOhlcvAll();
    }

    /**
     * 월봉 수집
     * 매월 1일 3:00에 실행
     */
    // todo : 테스트
//    @Scheduled(cron = "0 0 3 1 * *", zone = "Asia/Seoul")
    public void fetchMonthlyOhlcv() {
        log.info("[스케줄러] 월봉 수집 시작");

        kis1mOhlcvUpdateService.updateOhlcvAll();
    }

    /**
     * 15분봉으로 1일봉 생성
     * 장 종료 후 실행 (매일 16시 10분 10초, 주말 제외)
     */
    @Scheduled(cron = "0 10 16 * * MON-FRI", zone = "Asia/Seoul")
    public void generateDailyOhlcvFrom15min() {
        log.info("[스케줄러] 15분봉 기반 일봉 생성 시작");
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDateTime start = today.atTime(9, 0);
        LocalDateTime end = today.atTime(15, 59);

        kisOhlcvUpdateService.generateDailyOhlcvFrom15min(start, end);
    }
}