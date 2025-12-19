package kr.co.yournews.apis.subscription.dto;

import kr.co.yournews.domain.user.type.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;

public class SubscriptionDto {

    public record AppleConfirmRequest(
            String productId,
            String transactionId
    ) { }

    public record AppleRestoreRequest(
            String productId,
            List<String> transactionIds
    ) { }

    public record GoogleConfirmRequest(
            String packageName,
            String productId,
            String purchaseToken
    ) { }

    public record Response(
            SubscriptionStatus status,
            LocalDateTime expireAt
    ) {
        public static Response of(SubscriptionStatus status, LocalDateTime expireAt) {
            return new Response(status, expireAt);
        }
    }
}
