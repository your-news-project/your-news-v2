package kr.co.yournews.domain.notification.repository.messageprocess;

import java.time.LocalDateTime;

public interface MessageProcessHourlyAggregation {
    LocalDateTime getSummaryStartedAt();

    long getTotalCount();

    long getSuccessCount();

    long getFailureCount();

    long getRetryCount();

    long getDlqFailedCount();

    LocalDateTime getFirstCreatedAt();

    LocalDateTime getLastCompletedAt();

    long getProcessingDurationSeconds();
}
