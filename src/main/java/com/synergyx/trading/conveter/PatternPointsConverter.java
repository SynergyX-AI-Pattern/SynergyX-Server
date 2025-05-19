package com.synergyx.trading.conveter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class PatternPointsConverter implements AttributeConverter<List<Double>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    // 엔티티 List<Double> → JSON 문자열(String) 변환 메서드
    // ex) → {0.0, 2.5, 3.1} -> "[0.0, 2.5, 3.1]"
    // JPA가 엔티티를 저장할 때 자동으로 호출
    // 리스트를 문자열로 직렬화해 DB에 저장
    // 파싱 실패 시 빈 리스트 반환
    @Override
    public String convertToDatabaseColumn(List<Double> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            return "[]";
        }
    }

    // DB에 저장된 JSON 좌표를 List<Double>로 파싱하는 메서드
    // 패턴 좌표는 DB에 JSON 형태로 저장
    // ex) "[0.0, 2.5, 3.1]" → {0.0, 2.5, 3.1}
    // 파싱 실패 시 빈 리스트 반환
    @Override
    public List<Double> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, new TypeReference<List<Double>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

