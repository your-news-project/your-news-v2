package kr.co.yournews.domain.notification.repository;

import kr.co.yournews.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long>, CustomNotificationRepository {
    Page<Notification> findAllByUser_Id(Long userId, Pageable pageable);
    Page<Notification> findAllByUser_IdAndIsRead(Long userId, boolean isRead, Pageable pageable);
}
