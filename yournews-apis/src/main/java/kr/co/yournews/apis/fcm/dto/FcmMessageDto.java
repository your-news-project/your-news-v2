package kr.co.yournews.apis.fcm.dto;

public record FcmMessageDto(
        String token,
        String title,
        String data,
        boolean isLast
) {
    public static FcmMessageDto of(String token, String title, String data, boolean isLast) {
        return new FcmMessageDto(token, title, data, isLast);
    }
}
