package kr.co.yournews.apis.notification.dto;

import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.type.NotificationType;

import java.util.List;

public class NotificationDto {

    public record Summary(
            Long id,
            String newsName,
            String postTitle,
            boolean isRead,
            NotificationType type
    ) {
        public static Summary from(Notification notification) {
            return new Summary(
                    notification.getId(),
                    notification.getNewsName(),
                    notification.getPostTitle().get(0),
                    notification.isRead(),
                    notification.getType()
            );
        }
    }

    public record Details(
            Long id,
            String newsName,
            List<String> postTitle,
            List<String> postUrl,
            boolean isRead,
            NotificationType type
    ) {
        public static Details from(Notification notification) {
            return new Details(
                    notification.getId(),
                    notification.getNewsName(),
                    notification.getPostTitle(),
                    notification.getPostUrl(),
                    notification.isRead(),
                    notification.getType()
            );
        }
    }
}
