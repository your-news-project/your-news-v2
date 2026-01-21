package kr.co.yournews.infra.iap.google;

import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import kr.co.yournews.common.sentry.SentryCapture;
import kr.co.yournews.infra.iap.dto.GooglePlaySubscriptionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GooglePlayClient {
    private final AndroidPublisher androidPublisher;

    /**
     * Google Play 구독 정보를 조회하는 메서드
     *
     * @param packageName   : 앱 패키지명
     * @param productId     : 구독 상품 ID
     * @param purchaseToken : 구매 토큰
     * @return : Google Play 구독 정보
     */
    public GooglePlaySubscriptionInfo getSubscription(
            String packageName,
            String productId,
            String purchaseToken
    ) {
        try {
            SubscriptionPurchase purchase = androidPublisher
                    .purchases()
                    .subscriptions()
                    .get(packageName, productId, purchaseToken)
                    .execute();

            return GooglePlaySubscriptionInfo.of(
                    purchase.getStartTimeMillis(),
                    purchase.getExpiryTimeMillis(),
                    purchase.getAutoRenewing(),
                    purchase.getPaymentState()
            );
        } catch (Exception e) {
            SentryCapture.warn(
                    "subscription",
                    Map.of(
                            "platform", "google",
                            "stage", "iap.getSubscription",
                            "reason", "lookup_failed"
                    ),
                    "[IAP][GOOGLE] subscription lookup failed"
            );

            throw new RuntimeException(
                    "Google Play subscription lookup failed productId= " + productId,
                    e
            );
        }
    }
}
