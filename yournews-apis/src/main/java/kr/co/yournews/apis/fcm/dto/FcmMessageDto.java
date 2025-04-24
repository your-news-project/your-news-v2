package kr.co.yournews.apis.fcm.dto;

public record FcmMessageDto(
        String token,
        String title,
        String content
) {
    public static FcmMessageDto of(String token, String title, String content) {
        return new FcmMessageDto(token, title, content);
    }
}
