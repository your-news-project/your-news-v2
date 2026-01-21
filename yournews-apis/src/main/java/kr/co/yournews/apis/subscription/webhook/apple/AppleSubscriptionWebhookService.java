package kr.co.yournews.apis.subscription.webhook.apple;

import kr.co.yournews.common.sentry.SentryCapture;
import kr.co.yournews.common.util.DateTimeConvertUtil;
import kr.co.yournews.domain.user.entity.Subscription;
import kr.co.yournews.domain.user.service.SubscriptionService;
import kr.co.yournews.domain.user.type.SubscriptionStatus;
import kr.co.yournews.infra.iap.apple.AppleAppStoreClient;
import kr.co.yournews.infra.iap.dto.AppleServerNotificationDto;
import kr.co.yournews.infra.iap.dto.AppleTransactionDecoded;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleSubscriptionWebhookService {
    private final SubscriptionService subscriptionService;
    private final AppleAppStoreClient appleAppStoreClient;

    @Value("${apple-iap.productId}")
    private String productId;

    /**
     * Apple App Store로부터 전달된 webhook 요청을 처리하는 메서드.
     * - webhook payload를 디코딩한 후,
     * - 알림 타입(notificationType)에 따라 기존 구독 상태를 갱신.
     *
     * @param body : Apple 서버로부터 전달받은 webhook 요청 본문
     */
    @Transactional
    public void handleAppleWebhook(String body) {
        AppleServerNotificationDto dto = appleAppStoreClient.decodeWebhookTransaction(body);
        String notificationType = dto.notificationType();
        String subtype = dto.subtype();
        AppleTransactionDecoded resultDto = dto.transaction();

        // 다른 상품의 웹훅 무시
        if (!productId.equals(resultDto.productId())) {
            log.info("[Apple Webhook] Ignore webhook for other product. productId={}", resultDto.productId());
            return;
        }

        String originalTransactionId = resultDto.originalTransactionId();
        Subscription subscription =
                subscriptionService.readTopBySubscriptionIdOrderByCreatedAtDesc(originalTransactionId)
                        .orElse(null);

        if (subscription == null) {
            SentryCapture.warn(
                    "subscription",
                    Map.of(
                            "platform", "apple",
                            "stage", "webhook",
                            "reason", "subscription_not_found"
                    ),
                    Map.of(
                            "notificationType", notificationType,
                            "subtype", subtype == null ? "null" : subtype
                    ),
                    "[APPLE][WEBHOOK] subscription not found"
            );
            log.warn("[Apple Webhook] No subscription found for originalTransactionId={}", originalTransactionId);
            return;
        }

        // 알림 타입별 구독 상태 처리
        switch (notificationType) {
            case "SUBSCRIBED" -> handleSubscribed(subtype, resultDto.expiresDate(), subscription);
            case "DID_RENEW" -> handleDidRenew(subtype, resultDto.expiresDate(), subscription);
            case "DID_CHANGE_RENEWAL_STATUS" -> handleDidChangeRenewalStatus(subtype, subscription);
            case "EXPIRED" -> handleExpired(subtype, resultDto.expiresDate(), subscription);
            case "DID_FAIL_TO_RENEW" -> handleDidFailToRenew(subtype, subscription);
            case "GRACE_PERIOD_EXPIRED" -> handleGracePeriodExpired(subscription);
            case "REFUND" -> handleRefund(subscription);
            case "REFUND_REVERSED" -> handleRefundReversed(subscription);
            default -> {
                SentryCapture.warn(
                        "subscription",
                        Map.of(
                                "platform", "apple",
                                "stage", "webhook",
                                "reason", "unhandled_notification"
                        ),
                        Map.of(
                                "notificationType", notificationType,
                                "subtype", subtype == null ? "null" : subtype
                        ),
                        "[APPLE][WEBHOOK] unhandled notificationType"
                );
                log.info("[Apple Webhook] Unhandled notificationType={}", notificationType);
            }
        }
    }

    /**
     * SUBSCRIBED
     * - subtype: INITIAL_BUY(최초 구독), RESUBSCRIBE(재구독)
     */
    private void handleSubscribed(
            String subtype,
            Long expiresDate,
            Subscription subscription
    ) {
        subscription.updateDatesAndStatus(
                DateTimeConvertUtil.epochMillisToLocalDateTime(expiresDate),
                SubscriptionStatus.ACTIVE
        );

        log.info("[Apple Webhook] SUBSCRIBED ({}) - subscriptionId={}",
                subtype, subscription.getSubscriptionId());
    }

    /**
     * DID_RENEW
     * - 자동 갱신 성공
     * - subtype: BILLING_RECOVERY (이전 실패 → 회복)
     */
    private void handleDidRenew(
            String subtype,
            Long expiresDate,
            Subscription subscription
    ) {
        subscription.updateDatesAndStatus(
                DateTimeConvertUtil.epochMillisToLocalDateTime(expiresDate),
                SubscriptionStatus.ACTIVE
        );

        if ("BILLING_RECOVERY".equals(subtype)) {
            log.info("[Apple Webhook] DID_RENEW (BILLING_RECOVERY) - revive subscriptionId={}",
                    subscription.getSubscriptionId());
        } else {
            log.info("[Apple Webhook] DID_RENEW - subscriptionId={}", subscription.getSubscriptionId());
        }
    }

    /**
     * DID_CHANGE_RENEWAL_STATUS
     * - subtype: AUTO_RENEW_DISABLED(구독 취소 예정), AUTO_RENEW_ENABLED(구독 취소 내역 재시작)
     */
    private void handleDidChangeRenewalStatus(String subtype, Subscription subscription) {
        if ("AUTO_RENEW_DISABLED".equals(subtype)) {
            subscription.updateStatus(SubscriptionStatus.ACTIVE_CANCEL_AT_PERIOD_END);
            log.info("[Apple Webhook] Auto-renew disabled - subscriptionId={}",
                    subscription.getSubscriptionId());
        } else if ("AUTO_RENEW_ENABLED".equals(subtype)) {
            subscription.updateStatus(SubscriptionStatus.ACTIVE);
            log.info("[Apple Webhook] Auto-renew enabled - subscriptionId={}",
                    subscription.getSubscriptionId());
        } else {
            log.info("[Apple Webhook] DID_CHANGE_RENEWAL_STATUS unknown subtype={} subscriptionId={}",
                    subtype, subscription.getSubscriptionId());
        }
    }

    /**
     * EXPIRED
     * - 구독 만료
     */
    private void handleExpired(
            String subtype,
            Long expiresDate,
            Subscription subscription
    ) {
        subscription.updateDatesAndStatus(
                DateTimeConvertUtil.epochMillisToLocalDateTime(expiresDate),
                SubscriptionStatus.EXPIRED
        );

        log.info("[Apple Webhook] EXPIRED (subtype={}) - subscriptionId={}",
                subtype, subscription.getSubscriptionId());
    }

    /**
     * DID_FAIL_TO_RENEW
     * - 결제 실패 → subtype: GRACE_PERIOD(결제 유예) or null(재시도)
     */
    private void handleDidFailToRenew(String subtype, Subscription subscription) {
        if ("GRACE_PERIOD".equals(subtype)) {
            subscription.updateStatus(SubscriptionStatus.GRACE);
            log.info("[Apple Webhook] DID_FAIL_TO_RENEW with GRACE_PERIOD - subscriptionId={}",
                    subscription.getSubscriptionId());
        } else {
            subscription.updateStatus(SubscriptionStatus.BILLING_RETRY);
            log.info("[Apple Webhook] DID_FAIL_TO_RENEW (no grace - retry) - subscriptionId={}",
                    subscription.getSubscriptionId());
        }
    }

    /**
     * GRACE_PERIOD_EXPIRED
     * - 유예기간 종료 → 구독 만료 확정
     */
    private void handleGracePeriodExpired(Subscription subscription) {
        subscription.updateStatus(SubscriptionStatus.EXPIRED);
        log.info("[Apple Webhook] GRACE_PERIOD_EXPIRED - subscriptionId={}", subscription.getSubscriptionId());
    }

    /**
     * REFUND
     * - 환불 승인 → 구독 환불 상태로 변경
     */
    private void handleRefund(Subscription subscription) {
        subscription.updateStatus(SubscriptionStatus.REFUNDED);
        log.info("[Apple Webhook] REFUND - subscriptionId={}", subscription.getSubscriptionId());
    }

    /**
     * REFUND_REVERSED
     * - 환불 취소 → 원래 상태로 변경
     */
    private void handleRefundReversed(Subscription subscription) {
        subscription.updateStatus(SubscriptionStatus.ACTIVE);
        log.info("[Apple Webhook] REFUND_REVERSED - subscriptionId={}", subscription.getSubscriptionId());
    }
}
