package kr.co.yournews.apis.news.dto;

import java.util.List;

public class SubNewsDto {

    public record Request(
            List<Long> ids
    ) { }

    public record Response() {}
}
