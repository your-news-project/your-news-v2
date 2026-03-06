package kr.co.yournews.domain.notification.service;

import kr.co.yournews.domain.notification.entity.NotificationOutboxDailySummary;
import kr.co.yournews.domain.notification.repository.outboxsummary.NotificationOutboxDailySummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationOutboxDailySummaryService {
    private final NotificationOutboxDailySummaryRepository notificationOutboxDailySummaryRepository;

    public NotificationOutboxDailySummary save(NotificationOutboxDailySummary summary) {
        return notificationOutboxDailySummaryRepository.save(summary);
    }
}
