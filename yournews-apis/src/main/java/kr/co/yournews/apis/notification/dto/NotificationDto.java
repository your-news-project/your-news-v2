package kr.co.yournews.apis.notification.dto;

import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.type.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationDto {

    public record Summary(
            Long id,
            String newsName,
            boolean isRead,
            NotificationType type,
            LocalDateTime createdAt
    ) {
        public static Summary from(Notification notification) {
            return new Summary(
                    notification.getId(),
                    notification.getNewsName(),
                    notification.isRead(),
                    notification.getType(),
                    notification.getCreatedAt()
            );
        }
    }

    public record Details(
            Long id,
            String newsName,
            List<String> postTitle,
            List<String> postUrl,
            boolean isRead,
            NotificationType type,
            LocalDateTime createdAt
    ) {
        public static Details from(Notification notification) {
            return new Details(
                    notification.getId(),
                    notification.getNewsName(),
                    notification.getPostTitle(),
                    notification.getPostUrl(),
                    notification.isRead(),
                    notification.getType(),
                    notification.getCreatedAt()
            );
        }
    }

    public record DeleteRequest(
            List<Long> notificationIds
    ) { }
}
