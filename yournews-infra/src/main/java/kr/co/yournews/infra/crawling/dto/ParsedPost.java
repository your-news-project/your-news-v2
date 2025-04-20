package kr.co.yournews.infra.crawling.dto;

public record ParsedPost(
        String title,
        String url
) {
    public static ParsedPost of(String title, String url) {
        return new ParsedPost(title, url);
    }
}
