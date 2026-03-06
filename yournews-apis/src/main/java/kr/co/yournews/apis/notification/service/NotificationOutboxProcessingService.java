package kr.co.yournews.apis.notification.service;

import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import kr.co.yournews.domain.notification.service.NotificationOutboxService;
import kr.co.yournews.domain.notification.type.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationOutboxProcessingService {
    private final NotificationOutboxService notificationOutboxService;
    private final NotificationOutboxResultHandler outboxResultHandler;

    private static final int BATCH_SIZE = 200;
    private static final int CONFIRM_TIMEOUT_SECONDS = 30;

    /**
     * confirm timeout 지난 IN_PROGRESS 메시지 복구 메서드
     * - IN_PROGRESS 상태 && nextAttemptAt < now
     * - retry or fail 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recoverExpiredInProgressMessages(LocalDateTime now) {
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

    /**
     * 발행 가능한 PENDING 메시지를 선점(claim) 메서드
     * - PENDING 조회
     * - attemptCount 증가
     * - status -> IN_PROGRESS
     * - nextAttemptAt -> confirm timeout 기준 설정
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<NotificationOutbox> claimPendingMessages(LocalDateTime now) {
        List<NotificationOutbox> targets = notificationOutboxService.readPendingForPublish(
                OutboxStatus.PENDING,
                now,
                PageRequest.of(0, BATCH_SIZE)
        );

        if (targets.isEmpty()) {
            return targets;
        }

        LocalDateTime confirmTimeoutAt =
                now.plusSeconds(CONFIRM_TIMEOUT_SECONDS);

        for (NotificationOutbox outbox : targets) {
            outbox.incrementAttempt();
            outbox.markInProgress(confirmTimeoutAt);
        }

        return targets;
    }

    /**
     * 메시지 발행(send) 단계 예외 처리 메서드
     * - 별도 트랜잭션에서 outbox를 재조회해 재시도/실패 전이한다.
     *
     * @param outboxId  : outbox id
     * @param throwable : 발행 예외
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePublishException(Long outboxId, Throwable throwable) {
        if (outboxId == null) {
            return;
        }

        NotificationOutbox outbox = notificationOutboxService.readById(outboxId)
                .orElse(null);

        if (outbox == null) {
            return;
        }

        outboxResultHandler.handlePublishException(outbox, throwable);
    }
}
