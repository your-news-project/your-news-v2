package kr.co.yournews.domain.notification.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import kr.co.yournews.domain.notification.entity.QNotificationOutbox;
import kr.co.yournews.domain.notification.type.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CustomNotificationOutboxRepositoryImpl implements CustomNotificationOutboxRepository {
    private static final int BATCH_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;
    private final JPAQueryFactory jpaQueryFactory;

    private final QNotificationOutbox notificationOutbox = QNotificationOutbox.notificationOutbox;

    @Override
    public void saveAllInBatch(List<NotificationOutbox> outboxes) {
        String sql = "INSERT INTO notification_outbox ("
                + "exchange_name, routing_key, payload, status, attempt_count, max_attempt_count, next_attempt_at, last_error, created_at, updated_at"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.batchUpdate(
                sql,
                outboxes,
                BATCH_SIZE,
                (PreparedStatement ps, NotificationOutbox outbox) -> {
                    ps.setString(1, outbox.getExchangeName());
                    ps.setString(2, outbox.getRoutingKey());
                    ps.setString(3, outbox.getPayload());
                    ps.setString(4, outbox.getStatus().name());
                    ps.setInt(5, outbox.getAttemptCount());
                    ps.setInt(6, outbox.getMaxAttemptCount());
                    ps.setNull(7, Types.TIMESTAMP);
                    ps.setNull(8, Types.LONGVARCHAR);
                    ps.setTimestamp(9, Timestamp.valueOf(now));
                    ps.setTimestamp(10, Timestamp.valueOf(now));
                }
        );
    }

    /**
     * 메시지 발행 대기 상태 조회
     */
    @Override
    public List<NotificationOutbox> findPendingForPublish(OutboxStatus status, LocalDateTime dateTime, Pageable pageable) {
        return jpaQueryFactory
                .selectFrom(notificationOutbox)
                .where(
                        notificationOutbox.status.eq(status)
                                .and(
                                        notificationOutbox.nextAttemptAt.isNull()
                                                .or(notificationOutbox.nextAttemptAt.loe(dateTime))
                                )
                )
                .orderBy(notificationOutbox.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * 메시지 처리 중 시간이 만료된 데이터 조회
     */
    @Override
    public List<NotificationOutbox> findExpiredInProgressForRecovery(
            OutboxStatus status,
            LocalDateTime dateTime,
            Pageable pageable
    ) {
        return jpaQueryFactory
                .selectFrom(notificationOutbox)
                .where(
                        notificationOutbox.status.eq(status),
                        notificationOutbox.nextAttemptAt.isNotNull(),
                        notificationOutbox.nextAttemptAt.loe(dateTime)
                )
                .orderBy(notificationOutbox.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
