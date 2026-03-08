package kr.co.yournews.apis.crawling.strategy.dto;

import kr.co.yournews.domain.notification.entity.Notification;

import java.util.List;
import java.util.Map;

public record NotificationPrepareResult(
        List<Notification> notifications,
        Map<Long, List<String>> userIdToTitles
) {
}
