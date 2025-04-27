package kr.co.yournews.apis.notification.scheduler;

import kr.co.yournews.apis.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final NotificationCommandService notificationCommandService;

    /**
     * 매일 자정(00:00:00)에 실행되어
     * 10일이 지난 알림들을 삭제하는 스케줄링 메서드
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldNotifications() {
        notificationCommandService.deleteOldNotification();
    }
}
