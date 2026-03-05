package kr.co.yournews.apis.notification.service;

import kr.co.yournews.common.sentry.SentryCapture;
import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import kr.co.yournews.domain.notification.service.NotificationOutboxService;
import kr.co.yournews.infra.rabbitmq.RabbitPublishConfirmHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOutboxResultHandler implements RabbitPublishConfirmHandler {
    private final NotificationOutboxService notificationOutboxService;

    /**
     * RabbitMQ publish confirm(ack/nack) 결과를 처리 메서드
     *
     * @param correlationId : correlation data id(outbox id)
     * @param ack           : publish ack 여부
     * @param cause         : nack 원인
     */
    @Override
    @Transactional
    public void handleConfirm(String correlationId, boolean ack, String cause) {
        Long outboxId = parseOutboxId(correlationId);
        if (outboxId == null) {
            return;
        }

        Optional<NotificationOutbox> target = notificationOutboxService.readById(outboxId);
        if (target.isEmpty()) {
            return;
        }

        NotificationOutbox outbox = target.get();
        if (ack) {
            // ack 수신 시 SENT 확정
            outbox.markSent();
            log.info("[알림 아웃박스 전송 확인 완료] outboxId: {}, exchange: {}, routingKey: {}",
                    outbox.getId(), outbox.getExchangeName(), outbox.getRoutingKey());
            return;
        }

        // nack 처리. 재시도/실패 처리
        String errorMessage = cause == null ? "rabbitmq_publish_nack" : cause;
        retryOrFail(outbox, errorMessage, "publisher_confirm", "nack_max_retry_exceeded", "(CONFIRM)");
    }

    /**
     * RabbitMQ returned message 결과를 처리 메서드
     *
     * @param correlationId : correlation data id(outbox id)
     * @param reason        : returned 사유
     */
    @Override
    @Transactional
    public void handleReturned(String correlationId, String reason) {
        Long outboxId = parseOutboxId(correlationId);
        if (outboxId == null) {
            return;
        }

        Optional<NotificationOutbox> target = notificationOutboxService.readById(outboxId);
        if (target.isEmpty()) {
            return;
        }

        NotificationOutbox outbox = target.get();

        // 재시도/실패 처리
        String errorMessage = reason == null ? "rabbitmq_publish_returned" : reason;
        retryOrFail(outbox, errorMessage, "publisher_returned", "returned_max_retry_exceeded", "(RETURNED)");
    }

    /**
     * 발행 요청(send) 단계 예외를 처리 메서드
     *
     * @param outbox    : 처리 대상 outbox
     * @param throwable : 발생 예외
     */
    public void handlePublishException(NotificationOutbox outbox, Throwable throwable) {
        String errorMessage = getErrorMessage(throwable);
        retryOrFail(outbox, errorMessage, "publish_request", "publish_exception_max_retry_exceeded", "(SEND)");
    }

    /**
     * confirm timeout으로 정체된 IN_PROGRESS 처리 메서드
     *
     * @param outbox : 처리 대상 outbox
     */
    public void handleConfirmTimeout(NotificationOutbox outbox) {
        retryOrFail(outbox, "publish confirm timeout", "confirm_timeout", "confirm_timeout_max_retry_exceeded", "(TIMEOUT)");
    }

    /**
     * 재시도/최종 실패 전이 로직
     * - 최대 시도 횟수 도달 시 FAILED, 아니면 backoff를 적용해 PENDING으로 전이한다.
     */
    private void retryOrFail(
            NotificationOutbox outbox,
            String errorMessage,
            String stage,
            String reason,
            String logQualifier
    ) {
        // 최대 시도 횟수 도달 시 최종 실패 처리
        if (outbox.getAttemptCount() >= outbox.getMaxAttemptCount()) {
            outbox.markFailed(errorMessage);

            log.error("[알림 아웃박스 최종 실패{}] outboxId: {}, attempt: {}, reason: {}",
                    logQualifier, outbox.getId(), outbox.getAttemptCount(), errorMessage);
            captureFinalFailure(stage, reason, outbox, errorMessage);
            return;
        }

        // 재시도 가능하면 backoff 적용 후 PENDING으로 복귀
        long backoffMs = calcBackoffMs(outbox.getAttemptCount());
        outbox.markRetry(errorMessage, LocalDateTime.now().plus(Duration.ofMillis(backoffMs)));

        log.warn("[알림 아웃박스 재시도 예약{}] outboxId: {}, attempt: {}, nextAttemptAt: {}, reason: {}",
                logQualifier, outbox.getId(), outbox.getAttemptCount(), outbox.getNextAttemptAt(), errorMessage);
    }

    /**
     * 시도 횟수 기반 backoff 계산 메서드
     *
     * @param attempt : 현재 시도 횟수
     * @return : 다음 재시도까지 대기 시간(ms)
     */
    private long calcBackoffMs(int attempt) {
        return 3000L * (1L << Math.max(0, attempt - 1));
    }

    /**
     * 예외 객체에서 로그/저장용 에러 메시지를 추출 메서드
     *
     * @param throwable : 예외
     * @return : 에러 메시지
     */
    private String getErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "unknown";
        }

        return throwable.getMessage() == null ? throwable.getClass().getName() : throwable.getMessage();
    }

    /**
     * correlation id 문자열을 outbox id(Long)로 파싱 메서드
     *
     * @param correlationId : correlation id
     * @return : outbox id, 파싱 실패 시 null
     */
    private Long parseOutboxId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(correlationId);
        } catch (NumberFormatException ignore) {
            return null;
        }
    }

    /**
     * 재시도 소진으로 최종 실패한 아웃박스 이벤트를 Sentry로 전송 메서드
     */
    private void captureFinalFailure(String stage, String reason, NotificationOutbox outbox, String errorMessage) {
        SentryCapture.warn(
                "rabbitmq_publish",
                Map.of(
                        "component", "rabbitmq",
                        "stage", stage,
                        "reason", reason
                ),
                Map.of(
                        "outboxId", outbox.getId(),
                        "exchange", outbox.getExchangeName(),
                        "routingKey", outbox.getRoutingKey(),
                        "attemptCount", outbox.getAttemptCount(),
                        "maxAttemptCount", outbox.getMaxAttemptCount(),
                        "errorMessage", errorMessage == null ? "null" : errorMessage
                ),
                "[NOTIFICATION][OUTBOX] publish max retry exceeded"
        );
    }
}
