package kr.co.yournews.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.co.yournews.common.BaseTimeEntity;
import kr.co.yournews.domain.notification.type.OutboxStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Entity(name = "notification_outbox")
public class NotificationOutbox extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exchange_name", nullable = false)
    private String exchangeName;

    @Column(name = "routing_key", nullable = false)
    private String routingKey;

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "max_attempt_count", nullable = false)
    private int maxAttemptCount;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Builder
    public NotificationOutbox(
            String exchangeName,
            String routingKey,
            String payload,
            OutboxStatus status,
            int attemptCount,
            int maxAttemptCount,
            LocalDateTime nextAttemptAt,
            String lastError
    ) {
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.payload = payload;
        this.status = status;
        this.attemptCount = attemptCount;
        this.maxAttemptCount = maxAttemptCount;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = lastError;
    }

    public void incrementAttempt() {
        this.attemptCount += 1;
    }

    public void markInProgress(LocalDateTime nextAttemptAt) {
        this.status = OutboxStatus.IN_PROGRESS;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = null;
    }

    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.nextAttemptAt = null;
        this.lastError = null;
    }

    public void markRetry(String errorMessage, LocalDateTime nextAttemptAt) {
        this.status = OutboxStatus.PENDING;
        this.nextAttemptAt = nextAttemptAt;
        this.lastError = errorMessage;
    }

    public void markFailed(String errorMessage) {
        this.status = OutboxStatus.FAILED;
        this.nextAttemptAt = null;
        this.lastError = errorMessage;
    }
}
