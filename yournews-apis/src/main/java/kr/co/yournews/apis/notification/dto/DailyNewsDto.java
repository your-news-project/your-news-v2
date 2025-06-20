package kr.co.yournews.apis.notification.dto;

public record DailyNewsDto(
        String title,
        String url
) {
    public static DailyNewsDto of(String title, String url) {
        return new DailyNewsDto(title, url);
    }
}
