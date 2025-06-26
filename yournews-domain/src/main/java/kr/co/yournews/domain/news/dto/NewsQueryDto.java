package kr.co.yournews.domain.news.dto;

import kr.co.yournews.domain.news.type.College;

public record NewsQueryDto(
        Long id,
        String name,
        String url,
        College college,
        Long subMember
) {
}
