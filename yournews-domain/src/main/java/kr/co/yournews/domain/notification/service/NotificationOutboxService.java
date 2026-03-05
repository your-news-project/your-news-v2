package kr.co.yournews.domain.notification.service;

import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import kr.co.yournews.domain.notification.repository.outbox.NotificationOutboxRepository;
import kr.co.yournews.domain.notification.type.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationOutboxService {
    private final NotificationOutboxRepository notificationOutboxRepository;

    public void saveAll(List<NotificationOutbox> outboxes) {
        notificationOutboxRepository.saveAllInBatch(outboxes);
    }

    public Optional<NotificationOutbox> readById(Long id) {
        return notificationOutboxRepository.findById(id);
    }

    public List<NotificationOutbox> readPendingForPublish(OutboxStatus status, LocalDateTime dateTime, Pageable pageable) {
        return notificationOutboxRepository.findPendingForPublish(status, dateTime, pageable);
    }

    public List<NotificationOutbox> readExpiredInProgressForRecovery(OutboxStatus status, LocalDateTime dateTime, Pageable pageable) {
        return notificationOutboxRepository.findExpiredInProgressForRecovery(status, dateTime, pageable);
    }

}
