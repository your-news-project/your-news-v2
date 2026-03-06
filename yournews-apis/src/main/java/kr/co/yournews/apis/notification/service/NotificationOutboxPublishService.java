package kr.co.yournews.apis.notification.service;

import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import kr.co.yournews.infra.rabbitmq.RabbitMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOutboxPublishService {
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final NotificationOutboxProcessingService outboxProcessingService;


    /**
     * PENDING 상태 아웃박스 메시지를 주기적으로 조회해 발행하는 메서드
     * - 처리 순서
     * 1) confirm timeout으로 정체된 IN_PROGRESS 복구
     * 2) 발행 가능한 PENDING 배치 조회
     * 3) 개별 메시지 발행 요청
     */
    @Scheduled(fixedDelay = 3000L)
    public void publishPendingMessages() {
        LocalDateTime now = LocalDateTime.now();

        // confirm timeout이 지난 IN_PROGRESS 건을 재시도/실패 상태로 정리
        outboxProcessingService.recoverExpiredInProgressMessages(now);

        // 현재 시점 기준 발행 가능한 PENDING 배치 조회
        List<NotificationOutbox> targets = outboxProcessingService.claimPendingMessages(now);

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
     *
     * @param outbox : 발행 대상 outbox 엔트리
     */
    private void publishMessage(NotificationOutbox outbox) {
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
            outboxProcessingService.handlePublishException(outbox.getId(), e);
        }
    }

}
