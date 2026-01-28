package kr.co.yournews.infra.rabbitmq;

import kr.co.yournews.common.sentry.SentryCapture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitCallbacks {
    private final RabbitTemplate rabbitTemplate;
    private final ThreadPoolTaskScheduler rabbitRetryScheduler;
    private final PendingPublishStore pendingPublishStore;

    private static final int MAX_RETRY = 3;     // 재시도 횟수

    /**
     * 브로커가 메시지를 exchange까지 정상 수신했는지(ACK/NACK)를 확인하는 메서드
     *
     * @param correlationData : 발행 시 지정한 메시지 식별 정보
     * @param ack             : 브로커 수신 성공 여부 (true: 성공, false: 실패)
     * @param cause           : NACK 발생 시 브로커에서 전달한 실패 사유
     */
    public void handleConfirm(CorrelationData correlationData, boolean ack, String cause) {
        String messageId = (correlationData == null ? null : correlationData.getId());
        if (messageId == null) return;

        PendingPublish pending = pendingPublishStore.get(messageId);
        if (pending == null) {
            return;
        }

        // 메시지 브로커에 발행 성공
        if (ack) {
            pendingPublishStore.remove(messageId);
            return;
        }

        String token = pending.getMessage().token();

        // NACK. 재시도 처리
        int attempt = pending.incrementAttempt();
        log.warn("RabbitMQ NACK token: {}, attempt: {}, cause: {}", token, attempt, cause);

        // 재시도 횟수 초과. 실패 처리
        if (attempt > MAX_RETRY) {
            pendingPublishStore.remove(messageId);
            log.error("RabbitMQ publish permanently failed token: {}, cause: {}", token, cause);

            SentryCapture.warn(
                    "rabbitmq_publish",
                    Map.of(
                            "component", "rabbitmq",
                            "stage", "publisher_confirm",
                            "reason", "nack_max_retry_exceeded"
                    ),
                    Map.of(
                            "messageId", messageId,
                            "token", token,
                            "cause", cause == null ? "null" : cause
                    ),
                    "[RABBITMQ][PUBLISH] NACK max retry exceeded"
            );

            return;
        }

        // 재시도 스케줄 등록
        long backoffMs = calcBackoffMs(attempt);
        rabbitRetryScheduler.schedule(
                () -> retryPublish(messageId),
                Instant.now().plusMillis(backoffMs)
        );
    }

    /**
     * NACK 발생 후, 백오프 시간이 지난 뒤 재발행을 수행하는 메서드
     *
     * @param messageId : 재시도 대상 메시지를 식별하기 위한 고유 ID
     */
    private void retryPublish(String messageId) {
        PendingPublish pending = pendingPublishStore.get(messageId);
        if (pending == null) return;

        CorrelationData correlationData = new CorrelationData(messageId);

        rabbitTemplate.convertAndSend(
                pending.getExchange(),
                pending.getRoutingKey(),
                pending.getMessage(),
                correlationData
        );
    }

    /**
     * 재시도 시간 간격 반환 메서드
     */
    private long calcBackoffMs(int attempt) {
        // 200ms, 400ms, 800ms
        return 200L * (1L << Math.max(0, attempt - 1));
    }
}
