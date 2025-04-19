package kr.co.yournews.apis.news.dto;

import kr.co.yournews.domain.news.entity.SubNews;

import java.util.List;

public class SubNewsDto {

    public record Request(
            List<Long> ids,
            List<String> keywords
    ) { }

    public record Response(
            String newsName
    ) {
        public static Response from(SubNews subNews) {
            return new Response(subNews.getNewsName());
        }
    }
}
