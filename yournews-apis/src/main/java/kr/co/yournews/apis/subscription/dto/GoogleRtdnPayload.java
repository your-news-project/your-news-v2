package kr.co.yournews.apis.subscription.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleRtdnPayload(
        String version,
        String packageName,
        String eventTimeMillis,
        TestNotification testNotification,
        SubscriptionNotification subscriptionNotification
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TestNotification(String version) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubscriptionNotification(
            Integer notificationType,
            String purchaseToken,
            String subscriptionId
    ) {}
}
