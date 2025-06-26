package kr.co.yournews.admin.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.yournews.domain.news.entity.News;
import kr.co.yournews.domain.news.type.College;

public record NewsReq(
        @NotBlank(message = "소식 제목은 필수 입력 값입니다.")
        String name,
        @NotBlank(message = "소식 주소는 필수 입력 값입니다.")
        String url,
        @NotNull(message = "단과대학은 필수 입력 값입니다.")
        College college
) {
    public News toEntity() {
        return News.builder()
                .name(name)
                .url(url)
                .college(college)
                .build();
    }
}
