package kr.co.yournews.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.co.yournews.common.BaseTimeEntity;
import kr.co.yournews.domain.notification.type.MessageProcessStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "message_process")
public class MessageProcess extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageProcessStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "max_attempt_count", nullable = false)
    private int maxAttemptCount;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_error_code", length = 64)
    private String lastErrorCode;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    @Column(name = "dlq_attempt_count", nullable = false)
    private int dlqAttemptCount;

    @Builder
    public MessageProcess(
            String idempotencyKey,
            String tokenHash,
            MessageProcessStatus status,
            int attemptCount,
            int maxAttemptCount,
            LocalDateTime processingStartedAt,
            LocalDateTime completedAt,
            String lastErrorCode,
            String lastErrorMessage,
            int dlqAttemptCount
    ) {
        this.idempotencyKey = idempotencyKey;
        this.tokenHash = tokenHash;
        this.status = status;
        this.attemptCount = attemptCount;
        this.maxAttemptCount = maxAttemptCount;
        this.processingStartedAt = processingStartedAt;
        this.completedAt = completedAt;
        this.lastErrorCode = lastErrorCode;
        this.lastErrorMessage = lastErrorMessage;
        this.dlqAttemptCount = dlqAttemptCount;
    }
}
