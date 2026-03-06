package kr.co.yournews.domain.notification.repository.outbox;

import java.time.LocalDate;

public interface NotificationOutboxDailyAggregation {
    LocalDate getSummaryDate();

    long getPublishCount();

    long getSuccessCount();

    long getFailureCount();

    long getRetryCount();
}
