package kr.co.yournews.domain.notification.repository.outbox;

import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long>, CustomNotificationOutboxRepository {
    @Modifying
    @Query("delete from notification_outbox n where n.createdAt < :dateTime")
    int deleteByCreatedAtBefore(@Param("dateTime") LocalDateTime dateTime);

    @Query(value = """
            SELECT DATE(created_at) AS summaryDate,
                   COUNT(*) AS publishCount,
                   SUM(CASE WHEN status = 'SENT' THEN 1 ELSE 0 END) AS successCount,
                   SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) AS failureCount,
                   SUM(CASE WHEN attempt_count > 1 THEN 1 ELSE 0 END) AS retryCount
            FROM notification_outbox
            WHERE created_at >= :startDateTime
              AND created_at < :endDateTime
            GROUP BY DATE(created_at)
            """, nativeQuery = true)
    Optional<NotificationOutboxDailyAggregation> findDailyAggregation(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
