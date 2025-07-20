package kr.co.yournews.apis.notification.dto;

public record NotificationRankingDto(
        String name,
        int score
) {
    public NotificationRankingDto of(String name, int score) {
        return new NotificationRankingDto(name, score);
    }
}
