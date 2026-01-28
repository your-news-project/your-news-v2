package kr.co.yournews.infra.rabbitmq.dto;

public record FcmMessageDto(
        String token,
        String title,
        String content,
        String target,
        String info,
        boolean isFirst,
        boolean isLast
) {
    public static FcmMessageDto of(
            String token, String title, String content, String target,
            String info, boolean isFirst, boolean isLast
    ) {
        return new FcmMessageDto(
                token, title, content, target,
                info, isFirst, isLast
        );
    }
}
