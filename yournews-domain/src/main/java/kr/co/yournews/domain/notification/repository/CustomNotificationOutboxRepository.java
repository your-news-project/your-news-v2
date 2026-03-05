package kr.co.yournews.domain.notification.repository;

import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import kr.co.yournews.domain.notification.type.OutboxStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomNotificationOutboxRepository {
    void saveAllInBatch(List<NotificationOutbox> outboxes);
    List<NotificationOutbox> findPendingForPublish(OutboxStatus status, LocalDateTime dateTime, Pageable pageable);
    List<NotificationOutbox> findExpiredInProgressForRecovery(OutboxStatus status, LocalDateTime dateTime, Pageable pageable);
}
