package kr.co.yournews.infra.iap.dto;

public record GooglePlaySubscriptionInfo(
        Long purchaseDate,
        Long expiresDate,
        Boolean autoRenewing,
        Integer paymentState
) {
    public static GooglePlaySubscriptionInfo of(
            Long purchaseDate,
            Long expiresDate,
            Boolean autoRenewing,
            Integer paymentState
    ) {
        return new GooglePlaySubscriptionInfo(
                purchaseDate,
                expiresDate,
                autoRenewing,
                paymentState
        );
    }
}
