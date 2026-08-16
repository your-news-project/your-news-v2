package kr.co.yournews.domain.studentservice.converter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentServiceImageUrlConverterTest {
    private final StudentServiceImageUrlConverter converter = new StudentServiceImageUrlConverter();

    @Test
    void convertsImageUrlListToJsonAndBack() {
        List<String> imageUrls = List.of(
                "https://bucket.s3.ap-northeast-2.amazonaws.com/student-services/one.jpg",
                "https://bucket.s3.ap-northeast-2.amazonaws.com/student-services/two.png"
        );

        String json = converter.convertToDatabaseColumn(imageUrls);
        List<String> restored = converter.convertToEntityAttribute(json);

        assertThat(restored).containsExactlyElementsOf(imageUrls);
    }

    @Test
    void handlesEmptyDatabaseValueAsEmptyList() {
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
    }
}
