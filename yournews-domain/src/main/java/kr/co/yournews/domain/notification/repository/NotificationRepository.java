package kr.co.yournews.domain.notification.repository;

import kr.co.yournews.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long>, CustomNotificationRepository {
    Optional<Notification> findByUserIdAndPublicId(Long userId, String publicId);
    Page<Notification> findAllByUserId(Long userId, Pageable pageable);
    Page<Notification> findAllByUserIdAndIsRead(Long userId, boolean isRead, Pageable pageable);
}
