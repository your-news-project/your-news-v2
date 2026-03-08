package kr.co.yournews.apis.notification.service;

import kr.co.yournews.apis.notification.constant.FcmTarget;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.user.entity.FcmToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationDispatchService {
    private final NotificationCommandService notificationCommandService;
    private final NotificationOutboxEnqueueService notificationOutboxEnqueueService;

    /**
     * 알림 데이터 저장 및 Outbox 저장
     * - 즉시 알림용
     */
    @Transactional
    public void saveNotificationsAndEnqueueOutbox(
            List<Notification> notifications,
            List<FcmToken> tokens,
            Map<Long, List<String>> userIdToTitles,
            String title,
            String publicId
    ) {
        if (notifications.isEmpty() || tokens.isEmpty()) {
            return;
        }

        notificationCommandService.createNotifications(notifications);

        notificationOutboxEnqueueService.enqueueMessages(
                tokens,
                userIdToTitles,
                title,
                FcmTarget.NOTIFICATION,
                publicId
        );
    }

    /**
     * 알림 데이터 저장 및 Outbox 저장
     * - 일간 알림용
     */
    @Transactional
    public void saveNotificationsAndEnqueueOutbox(
            List<Notification> notifications,
            List<FcmToken> tokens,
            String title,
            String content,
            String publicId
    ) {
        if (notifications.isEmpty() || tokens.isEmpty()) {
            return;
        }

        notificationCommandService.createNotifications(notifications);

        notificationOutboxEnqueueService.enqueueMessages(
                tokens,
                title,
                content,
                FcmTarget.NOTIFICATION,
                publicId
        );
    }
}
