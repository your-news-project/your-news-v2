package kr.co.yournews.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.co.yournews.common.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "message_process_hourly_summary")
public class MessageProcessHourlySummary extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "summary_started_at", nullable = false, unique = true)
    private LocalDateTime summaryStartedAt;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "success_count", nullable = false)
    private int successCount;

    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "dlq_failed_count", nullable = false)
    private int dlqFailedCount;

    @Column(name = "first_created_at")
    private LocalDateTime firstCreatedAt;

    @Column(name = "last_completed_at")
    private LocalDateTime lastCompletedAt;

    @Column(name = "processing_duration_seconds", nullable = false)
    private long processingDurationSeconds;

    @Builder
    public MessageProcessHourlySummary(
            LocalDateTime summaryStartedAt,
            int totalCount,
            int successCount,
            int failureCount,
            int retryCount,
            int dlqFailedCount,
            LocalDateTime firstCreatedAt,
            LocalDateTime lastCompletedAt,
            long processingDurationSeconds
    ) {
        this.summaryStartedAt = summaryStartedAt;
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.retryCount = retryCount;
        this.dlqFailedCount = dlqFailedCount;
        this.firstCreatedAt = firstCreatedAt;
        this.lastCompletedAt = lastCompletedAt;
        this.processingDurationSeconds = processingDurationSeconds;
    }
}
