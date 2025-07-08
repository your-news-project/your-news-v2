package kr.co.yournews.domain.notification.repository;

import kr.co.yournews.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long>, CustomNotificationRepository {
    Optional<Notification> findByUserIdAndPublicId(Long userId, String publicId);
    Page<Notification> findAllByUserId(Long userId, Pageable pageable);
    Page<Notification> findAllByUserIdAndIsRead(Long userId, boolean isRead, Pageable pageable);
    Long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("DELETE FROM notification n WHERE n.createdAt < :dateTime")
    void deleteByDateTimeBefore(@Param("dateTime") LocalDateTime dateTime);
}
