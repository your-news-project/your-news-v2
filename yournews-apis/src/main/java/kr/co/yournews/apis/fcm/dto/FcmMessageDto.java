package kr.co.yournews.apis.fcm.dto;

public record FcmMessageDto(
        String token,
        String title,
        String data
) {
    public static FcmMessageDto of(String token, String title, String data) {
        return new FcmMessageDto(token, title, data);
    }
}
