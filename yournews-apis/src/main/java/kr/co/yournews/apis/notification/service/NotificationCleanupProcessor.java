package kr.co.yournews.apis.notification.service;

import kr.co.yournews.domain.notification.service.MessageProcessService;
import kr.co.yournews.domain.notification.service.NotificationOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupProcessor {
    private final MessageProcessService messageProcessService;
    private final NotificationOutboxService notificationOutboxService;

    /**
     * 2일이 지난 알림 아웃박스 및 메시지 처리 데이터 정리 메서드
     */
    @Transactional
    public void deleteNotificationProcessOlderThanTwoDays() {
        LocalDateTime cutoffDateTime = LocalDate.now().minusDays(2).atStartOfDay();

        int deletedMessageProcessCount = messageProcessService.deleteByCreatedAtBefore(cutoffDateTime);
        int deletedOutboxCount = notificationOutboxService.deleteByCreatedAtBefore(cutoffDateTime);

        log.info(
                "[알림 원본 데이터 삭제 완료] cutoffDateTime: {}, messageProcessCount: {}, outboxCount: {}",
                cutoffDateTime,
                deletedMessageProcessCount,
                deletedOutboxCount
        );
    }
}
