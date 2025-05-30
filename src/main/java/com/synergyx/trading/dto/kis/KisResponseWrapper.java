package com.synergyx.trading.dto.kis;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class KisResponseWrapper {
    private List<Map<String, String>> output;

    public List<KisStockDTO> toDtoList() {
        return output.stream().map(data -> {
            KisStockDTO dto = new KisStockDTO();
            dto.setSymbol(data.get("stck_shrn_iscd"));
            dto.setName(data.get("hts_kor_isnm"));
            dto.setPrice(Float.parseFloat(data.get("stck_prpr")));
            dto.setOpen(Float.parseFloat(data.get("stck_oprc")));
            dto.setHigh(Float.parseFloat(data.get("stck_hgpr")));
            dto.setLow(Float.parseFloat(data.get("stck_lwpr")));
            dto.setClose(Float.parseFloat(data.get("stck_clpr")));
            dto.setVolume(Integer.parseInt(data.get("acml_vol")));
            dto.setTimestamp(LocalDateTime.now());
            return dto;
        }).collect(Collectors.toList());
    }
}