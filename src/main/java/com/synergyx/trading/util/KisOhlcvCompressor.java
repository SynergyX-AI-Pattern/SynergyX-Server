package com.synergyx.trading.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class KisOhlcvCompressor {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File("src/main/resources/data/1m_sample_kis_response.json"));
        JsonNode candles = root.path("output2");

        Map<LocalDateTime, List<JsonNode>> grouped = new TreeMap<>();

        for (JsonNode candle : candles) {
            String date = candle.path("stck_bsop_date").asText();
            String hour = candle.path("stck_cntg_hour").asText();
            LocalDateTime ts = LocalDateTime.parse(date + hour, TS_FORMAT);

            LocalDateTime groupKey = (ts.getHour() == 15 && ts.getMinute() > 0)
                    ? LocalDateTime.of(ts.toLocalDate(), LocalTime.of(15, 0)) // 30분봉
                    : ts.withMinute(0).withSecond(0).withNano(0);             // 1시간봉

            grouped.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(candle);
        }

        for (Map.Entry<LocalDateTime, List<JsonNode>> entry : grouped.entrySet()) {
            LocalDateTime timestamp = entry.getKey();
            List<JsonNode> group = entry.getValue();

            int expectedCount = (timestamp.getHour() == 15 && timestamp.getMinute() == 0) ? 30 : 60;

            // 캔들 수 부족하면 skip
            if (group.size() < expectedCount) {
                log.warn("⚠️ [SKIP] 캔들 수 부족 ({}개 < {}개): {}", group.size(), expectedCount, timestamp);
                continue;
            }

            group.sort(Comparator.comparing(n -> LocalDateTime.parse(
                    n.path("stck_bsop_date").asText() + n.path("stck_cntg_hour").asText(), TS_FORMAT)));

            JsonNode first = group.get(0);
            JsonNode last = group.get(group.size() - 1);

            double open = Double.parseDouble(first.path("stck_oprc").asText());
            double close = Double.parseDouble(last.path("stck_prpr").asText());
            double high = group.stream().mapToDouble(n -> Double.parseDouble(n.path("stck_hgpr").asText())).max().orElse(0);
            double low = group.stream().mapToDouble(n -> Double.parseDouble(n.path("stck_lwpr").asText())).min().orElse(0);
            long volume = group.stream().mapToLong(n -> Long.parseLong(n.path("cntg_vol").asText())).sum();

            log.info("🕒 [{}] Open={}, High={}, Low={}, Close={}, Volume={}, Size={}",
                    timestamp, open, high, low, close, volume, group.size());
        }
    }
}
