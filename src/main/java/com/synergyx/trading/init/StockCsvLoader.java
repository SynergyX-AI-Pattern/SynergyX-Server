package com.synergyx.trading.init;

import com.synergyx.trading.model.Stock;
import com.synergyx.trading.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

//@Component // 실행 시 주석 해제
@RequiredArgsConstructor
public class StockCsvLoader implements CommandLineRunner {

    private final StockRepository stockRepository;

    /**
     * kospi100.csv에서 종목코드, 종목명을 읽어 stock 테이블에 저장합니다.
     *
     * @param args incoming main method arguments
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/kospi100.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("EUC-KR")));

        String line;
        boolean isFirst = true;

        while ((line = reader.readLine()) != null) {
            if (isFirst) {
                isFirst = false;
                continue;
            }

            String[] tokens = line.split(",");
            String symbol = tokens[0].trim().replace("\"", "");
            ; // 종목코드
            String name = tokens[1].trim().replace("\"", "");
            ;   // 종목명

            Stock stock = Stock.builder()
                    .symbol(symbol)
                    .name(name)
                    .imageUrl(null) // 임시 (크롤링 해야 함)
                    .build();

            stockRepository.save(stock);
        }
    }
}