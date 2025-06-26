package kr.co.yournews.admin.news.dto;

import kr.co.yournews.domain.news.dto.NewsQueryDto;
import kr.co.yournews.domain.news.entity.News;

public class NewsRes {

    public record Summary(
            Long id,
            String name,
            String url,
            String college
    ) {
        public static Summary from(News news) {
            return new Summary(
                    news.getId(), news.getName(),
                    news.getUrl(), news.getCollege().getLabel()
            );
        }
    }

    public record Details(
            Long id,
            String name,
            String url,
            String college,
            long subMember
    ) {
        public static Details from(NewsQueryDto news) {
            return new Details(
                    news.id(), news.name(), news.url(),
                    news.college().getLabel(), news.subMember()
            );
        }
    }
}
