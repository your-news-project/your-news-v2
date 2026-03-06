package kr.co.yournews.domain.notification.repository.outboxsummary;

import kr.co.yournews.domain.notification.entity.NotificationOutboxDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationOutboxDailySummaryRepository extends JpaRepository<NotificationOutboxDailySummary, Long> {
}
