package kr.co.yournews.apis.crawling.strategy.dto;

public record NewsDetail(
        String title,
        String content
) {
    public static NewsDetail of(String title, String content) {
        return new NewsDetail(title, content);
    }
}
