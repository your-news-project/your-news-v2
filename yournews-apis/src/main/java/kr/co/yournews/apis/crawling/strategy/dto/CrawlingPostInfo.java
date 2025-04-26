package kr.co.yournews.apis.crawling.strategy.dto;

import java.util.List;

public record CrawlingPostInfo(
        List<String> titles,
        List<String> urls
) {
    public boolean isEmpty() {
        return titles.isEmpty() || urls.isEmpty();
    }
}
