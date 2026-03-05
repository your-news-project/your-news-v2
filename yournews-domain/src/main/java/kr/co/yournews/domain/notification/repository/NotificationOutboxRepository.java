package kr.co.yournews.domain.notification.repository;

import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long>, CustomNotificationOutboxRepository {
}
