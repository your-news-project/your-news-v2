package kr.co.yournews.apis.news.dto;

import kr.co.yournews.domain.news.entity.News;

public record NewsInfoDto(
        Long id,
        String name,
        String college
) {
    public static NewsInfoDto from(News news) {
        return new NewsInfoDto(news.getId(), news.getName(), news.getCollege().getLabel());
    }
}
