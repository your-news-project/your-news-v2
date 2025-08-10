package kr.co.yournews.apis.notification.dto;

public record FcmMessageDto(
        String token,
        String title,
        String data,
        boolean isFirst,
        boolean isLast
) {
    public static FcmMessageDto of(String token, String title, String data, boolean isFirst, boolean isLast) {
        return new FcmMessageDto(token, title, data, isFirst, isLast);
    }
}
