package kr.co.yournews.domain.notification.repository;

import kr.co.yournews.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>, CustomNotificationRepository {
    Page<Notification> findAllByUserId(Long userId, Pageable pageable);
    Page<Notification> findAllByUserIdAndIsRead(Long userId, boolean isRead, Pageable pageable);
}
