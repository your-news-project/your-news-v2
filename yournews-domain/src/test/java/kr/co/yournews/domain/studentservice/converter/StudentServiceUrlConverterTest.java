package kr.co.yournews.domain.studentservice.converter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentServiceUrlConverterTest {
    private final StudentServiceUrlConverter converter = new StudentServiceUrlConverter();

    @Test
    void convertsUrlListToJsonAndBack() {
        List<String> urls = List.of(
                "https://example.com",
                "https://instagram.com/example"
        );

        String json = converter.convertToDatabaseColumn(urls);
        List<String> restored = converter.convertToEntityAttribute(json);

        assertThat(restored).containsExactlyElementsOf(urls);
    }

    @Test
    void handlesEmptyDatabaseValueAsEmptyList() {
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
    }
}
