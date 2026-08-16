package kr.co.yournews.domain.studentservice.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class StudentServiceUrlConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> urls) {
        try {
            return OBJECT_MAPPER.writeValueAsString(urls == null ? List.of() : urls);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("홍보 URL 직렬화에 실패했습니다.", exception);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String data) {
        if (data == null || data.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(OBJECT_MAPPER.readValue(data, new TypeReference<>() {}));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("홍보 URL 역직렬화에 실패했습니다.", exception);
        }
    }
}
