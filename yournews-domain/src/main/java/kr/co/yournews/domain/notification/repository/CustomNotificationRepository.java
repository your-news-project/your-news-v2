package kr.co.yournews.domain.notification.repository;

import kr.co.yournews.domain.notification.entity.Notification;

import java.util.List;

public interface CustomNotificationRepository {
    void saveAllInBatch(List<Notification> notifications);
}
