package kr.co.yournews.apis.notification.scheduler;

import kr.co.yournews.apis.notification.service.DailyNotificationProcessor;
import kr.co.yournews.apis.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final NotificationCommandService notificationCommandService;
    private final DailyNotificationProcessor dailyNotificationProcessor;

    /**
     * 매일 자정(00:00:00)에 실행되어
     * 10일이 지난 알림들을 삭제하는 스케줄링 메서드
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldNotifications() {
        log.info("[알림 삭제 스케줄 시작] 오래된 알림 삭제");
        notificationCommandService.deleteOldNotification();
    }

    /**
     * 매일 20:00에 실행되어
     * 일간 소식 정보를 전송하는 메서드
     */
    @Scheduled(cron = "0 0 20 * * *")
    public void runDailyNewsNotificationJob() {
        log.info("[일간 소식 전송 스케줄 시작] 일간 소식 전송");
        dailyNotificationProcessor.sendDailyNotification();
    }
}
