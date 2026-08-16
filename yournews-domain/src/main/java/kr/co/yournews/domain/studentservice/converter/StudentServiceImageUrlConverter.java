package kr.co.yournews.domain.studentservice.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class StudentServiceImageUrlConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> imageUrls) {
        try {
            return OBJECT_MAPPER.writeValueAsString(imageUrls == null ? List.of() : imageUrls);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("학생 서비스 이미지 URL 직렬화에 실패했습니다.", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String data) {
        if (data == null || data.isBlank()) {
            return new ArrayList<>();
        }

        try {
            return new ArrayList<>(OBJECT_MAPPER.readValue(data, new TypeReference<>() {}));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("학생 서비스 이미지 URL 역직렬화에 실패했습니다.", e);
        }
    }
}
