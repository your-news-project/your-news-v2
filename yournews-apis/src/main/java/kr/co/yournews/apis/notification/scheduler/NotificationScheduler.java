package kr.co.yournews.apis.notification.scheduler;

import kr.co.yournews.apis.notification.service.DailyNotificationProcessor;
import kr.co.yournews.apis.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
        notificationCommandService.deleteOldNotification();
    }

    /**
     * 매일 20:00에 실행되어
     * 일간 소식 정보를 전송하는 메서드
     */
    @Scheduled(cron = "0 0 20 * * *")
    public void runDailyNewsNotificationJob() {
        dailyNotificationProcessor.sendDailyNotification();
    }
}
