package kr.co.yournews.infra.iap.dto;

public record AppleServerNotificationDto(
        String notificationType,
        String subtype,
        AppleTransactionDecoded transaction
) {
    public static AppleServerNotificationDto of(
            String notificationType,
            String subtype,
            AppleTransactionDecoded transaction
    ) {
        return new AppleServerNotificationDto(notificationType, subtype, transaction);
    }
}
