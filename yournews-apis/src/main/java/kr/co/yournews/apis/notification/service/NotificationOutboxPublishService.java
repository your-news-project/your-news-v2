package kr.co.yournews.apis.notification.service;

import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import kr.co.yournews.domain.notification.service.NotificationOutboxService;
import kr.co.yournews.domain.notification.type.OutboxStatus;
import kr.co.yournews.infra.rabbitmq.RabbitMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOutboxPublishService {
    private final NotificationOutboxService notificationOutboxService;
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final NotificationOutboxResultHandler outboxResultHandler;

    private static final int BATCH_SIZE = 200;
    private static final int CONFIRM_TIMEOUT_SECONDS = 30;

    /**
     * PENDING 상태 아웃박스 메시지를 주기적으로 조회해 발행하는 메서드
     * - 처리 순서
     * 1) confirm timeout으로 정체된 IN_PROGRESS 복구
     * 2) 발행 가능한 PENDING 배치 조회
     * 3) 개별 메시지 발행 요청
     */
    @Transactional
    @Scheduled(fixedDelay = 3000L)
    public void publishPendingMessages() {
        LocalDateTime now = LocalDateTime.now();

        // confirm timeout이 지난 IN_PROGRESS 건을 재시도/실패 상태로 정리
        recoverExpiredInProgressMessages(now);

        // 현재 시점 기준 발행 가능한 PENDING 배치 조회
        List<NotificationOutbox> targets = notificationOutboxService.readPendingForPublish(
                OutboxStatus.PENDING,
                now,
                PageRequest.of(0, BATCH_SIZE)
        );

        if (targets.isEmpty()) {
            return;
        }

        for (NotificationOutbox outbox : targets) {
            // 건별 발행 요청
            publishMessage(outbox);
        }
    }

    /**
     * 단일 outbox 메시지를 발행 요청 메서드
     * - 발행 전 시도 횟수를 증가시키고 IN_PROGRESS로 전이
     * - 전송 중 예외 발생 시 즉시 예외 처리 핸들러로 위임한
     *
     * @param outbox : 발행 대상 outbox 엔트리
     */
    private void publishMessage(NotificationOutbox outbox) {
        // 발행 시도 카운트 증가
        outbox.incrementAttempt();
        // confirm timeout 기준 시각을 nextAttemptAt에 기록
        outbox.markInProgress(LocalDateTime.now().plusSeconds(CONFIRM_TIMEOUT_SECONDS));

        try {
            String messageId = String.valueOf(outbox.getId());
            rabbitMessagePublisher.send(
                    messageId,
                    outbox.getExchangeName(),
                    outbox.getRoutingKey(),
                    outbox.getPayload()
            );
            log.info("[알림 아웃박스 발행 요청] outboxId: {}, exchange: {}, routingKey: {}",
                    outbox.getId(), outbox.getExchangeName(), outbox.getRoutingKey());
        } catch (Exception e) {
            // send 단계 동기 예외는 즉시 재시도/실패 처리로 위임
            outboxResultHandler.handlePublishException(outbox, e);
        }
    }

    /**
     * timeout 기준을 지난 IN_PROGRESS 메시지를 복구 처리 메서드
     *
     * @param now : 현재 시각
     */
    private void recoverExpiredInProgressMessages(LocalDateTime now) {
        List<NotificationOutbox> stalled = notificationOutboxService.readExpiredInProgressForRecovery(
                OutboxStatus.IN_PROGRESS,
                now,
                PageRequest.of(0, BATCH_SIZE)
        );

        for (NotificationOutbox outbox : stalled) {
            // confirm timeout 경로로 재시도/최종 실패 처리
            outboxResultHandler.handleConfirmTimeout(outbox);
        }
    }
}
