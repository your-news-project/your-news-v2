package kr.co.yournews.apis.subscription.webhook.google;

import kr.co.yournews.apis.subscription.dto.GoogleRtdnPayload;
import kr.co.yournews.common.util.DateTimeConvertUtil;
import kr.co.yournews.domain.user.entity.Subscription;
import kr.co.yournews.domain.user.service.SubscriptionService;
import kr.co.yournews.domain.user.type.SubscriptionStatus;
import kr.co.yournews.infra.iap.dto.GooglePlaySubscriptionInfo;
import kr.co.yournews.infra.iap.google.GooglePlayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSubscriptionWebhookService {
    private final GoogleOidcVerifier googleOidcVerifier;
    private final GoogleRtdnPayloadParser googleRtdnPayloadParser;
    private final GooglePlayClient googlePlayClient;
    private final SubscriptionService subscriptionService;

    /**
     * Google로부터 전달된 webhook 요청을 처리하는 메서드.
     * - OIDC Bearer 토큰을 검증한 후 RTDN payload를 파싱하고,
     * - 구독 알림 타입에 따라 기존 구독 상태를 갱신.
     *
     * @param authorizationHeader : 토큰 헤더 (Bearer ...)
     * @param body                : webhook 요청 본문
     */
    @Transactional
    public void handleGoogleWebhook(String authorizationHeader, String body) {
        googleOidcVerifier.verifyBearerToken(authorizationHeader);

        GoogleRtdnPayload payload = googleRtdnPayloadParser.parse(body);

        // 테스트 알림은 무시
        if (payload.testNotification() != null) {
            return;
        }

        GoogleRtdnPayload.SubscriptionNotification notification = payload.subscriptionNotification();
        if (notification == null) {
            log.warn("[Google Webhook] subscriptionNotification is null. body={}", body);
            return;
        }

        Integer notificationType = notification.notificationType();
        String packageName = payload.packageName();
        String productId = notification.subscriptionId();
        String purchaseToken = notification.purchaseToken();

        // 필수 필드 누락 시 무시
        if (notificationType == null || packageName == null || productId == null || purchaseToken == null) {
            log.warn("[Google Webhook] missing fields. type={}, pkg={}, productId={}, tokenPresent={}",
                    notificationType, packageName, productId, (purchaseToken != null));
            return;
        }

        Subscription subscription = subscriptionService
                .readTopBySubscriptionIdOrderByCreatedAtDesc(purchaseToken)
                .orElse(null);

        if (subscription == null) {
            log.warn("[Google Webhook] No subscription found for purchaseToken={}", purchaseToken);
            return;
        }

        // Google Play API로 최신 구독 상태 조회
        GooglePlaySubscriptionInfo subscriptionInfo =
                googlePlayClient.getSubscription(packageName, productId, purchaseToken);

        LocalDateTime expireAt =
                DateTimeConvertUtil.epochMillisToLocalDateTime(subscriptionInfo.expiresDate());
        applyStatusByNotificationType(notificationType, expireAt, subscription);
    }

    /**
     * Google RTDN 알림 타입에 따라 구독 상태를 갱신하는 메서드.
     *
     * @param type         : RTDN notification type
     * @param expireAt     : 구독 만료 시각
     * @param subscription : 기존 구독 엔티티
     */
    private void applyStatusByNotificationType(
            int type,
            LocalDateTime expireAt,
            Subscription subscription
    ) {
        switch (type) {
            /*
             1: 계정 보류 복구
             2: 활성 정기 결제 갱신
             4: 정기 결제 구매
             7: 정기 결제 복원
             9: 갱신 기한 연
             */
            case 1, 2, 4, 7, 9 -> handleActivate(expireAt, subscription, type);
            // 취소
            case 3 -> handleCanceled(expireAt, subscription);
            // 결제 보류
            case 5 -> handleBillingRetry(subscription);
            // 유예
            case 6 -> handleGrace(subscription);
            // 환불
            case 12 -> handleRefunded(subscription);
            // 만료
            case 13 -> handleExpired(subscription);

            default -> log.info("[Google Webhook] another type={}", type);
        }
    }

    /**
     * ACTIVE 처리 공통
     */
    private void handleActivate(LocalDateTime expireAt, Subscription subscription, int type) {
        subscription.updateDatesAndStatus(expireAt, SubscriptionStatus.ACTIVE);
        log.info("[Google Webhook] ACTIVE (type={}) - subscriptionId={}",
                type, subscription.getSubscriptionId());
    }

    /**
     * CANCELED 공통
     */
    private void handleCanceled(LocalDateTime expireAt, Subscription subscription) {
        subscription.updateDatesAndStatus(expireAt, SubscriptionStatus.ACTIVE_CANCEL_AT_PERIOD_END);
        log.info("[Google Webhook] CANCEL_AT_PERIOD_END - subscriptionId={}", subscription.getSubscriptionId());
    }

    /**
     * 결제 보류(재시도)
     */
    private void handleBillingRetry(Subscription subscription) {
        subscription.updateStatus(SubscriptionStatus.BILLING_RETRY);
        log.info("[Google Webhook] BILLING_RETRY - subscriptionId={}", subscription.getSubscriptionId());
    }

    /**
     * 유예
     */
    private void handleGrace(Subscription subscription) {
        subscription.updateStatus(SubscriptionStatus.GRACE);
        log.info("[Google Webhook] GRACE - subscriptionId={}", subscription.getSubscriptionId());
    }

    /**
     * 환불
     */
    private void handleRefunded(Subscription subscription) {
        subscription.updateStatus(SubscriptionStatus.REFUNDED);
        log.info("[Google Webhook] REFUNDED - subscriptionId={}", subscription.getSubscriptionId());
    }

    /**
     * 만료
     */
    private void handleExpired(Subscription subscription) {
        subscription.updateStatus(SubscriptionStatus.EXPIRED);
        log.info("[Google Webhook] EXPIRED - subscriptionId={}", subscription.getSubscriptionId());
    }
}
