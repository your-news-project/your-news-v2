package kr.co.yournews.infra.iap.dto;

public record AppleTransactionDecoded(
        String originalTransactionId,
        String transactionId,
        String transactionReason,
        String productId,
        Long purchaseDate,
        Long expiresDate
) {
    public static AppleTransactionDecoded of(
            String originalTransactionId, String transactionId,
            String transactionReason, String productId,
            Long purchaseDate, Long expiresDate
    ) {
        return new AppleTransactionDecoded(
                originalTransactionId, transactionId,
                transactionReason, productId,
                purchaseDate, expiresDate
        );
    }
}
