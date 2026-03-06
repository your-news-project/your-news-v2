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

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "notification_outbox_daily_summary")
public class NotificationOutboxDailySummary extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "summary_date", nullable = false, unique = true)
    private LocalDate summaryDate;

    @Column(name = "publish_count", nullable = false)
    private int publishCount;

    @Column(name = "success_count", nullable = false)
    private int successCount;

    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Builder
    public NotificationOutboxDailySummary(
            LocalDate summaryDate,
            int publishCount,
            int successCount,
            int failureCount,
            int retryCount
    ) {
        this.summaryDate = summaryDate;
        this.publishCount = publishCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.retryCount = retryCount;
    }
}
