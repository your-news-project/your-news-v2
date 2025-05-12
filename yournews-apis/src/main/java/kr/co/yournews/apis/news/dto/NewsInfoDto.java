package kr.co.yournews.apis.news.dto;

import kr.co.yournews.domain.news.entity.News;

public class NewsInfoDto {

    public record Summary(
            Long id,
            String name,
            String url
    ) {
        public static Summary from(News news) {
            return new Summary(news.getId(), news.getName(), news.getUrl());
        }
    }

    public record Details(
            Long id,
            String name,
            String college
    ) {
        public static Details from(News news) {
            return new Details(news.getId(), news.getName(), news.getCollege().getLabel());
        }
    }
}
