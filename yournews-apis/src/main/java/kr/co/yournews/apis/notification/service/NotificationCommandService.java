package kr.co.yournews.apis.notification.service;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.exception.NotificationErrorType;
import kr.co.yournews.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationCommandService {
    private final NotificationService notificationService;

    /**
     * 알림 리스트를 모두 저장하는 메서드
     * - 알림 발송 후 DB에 저장할 때 사용
     *
     * @param notifications : 저장할 알림 목록
     */
    @Transactional
    public void createNotifications(List<Notification> notifications) {
        notificationService.saveAll(notifications);
    }

    /**
     * 특정 알림 id를 기반으로 알림을 삭제
     *
     * @param userId         : 사용자 pk
     * @param notificationId : 삭제할 알림의 pk
     */
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationService.readById(notificationId)
                .orElseThrow(() -> new CustomException(NotificationErrorType.NOT_FOUND));

        if (!notification.isReceiver(userId)) {
            throw new CustomException(NotificationErrorType.FORBIDDEN);
        }

        notificationService.deleteById(notificationId);
    }

    /**
     * 10일이 지난 알림 삭제
     */
    @Transactional
    public void deleteOldNotification() {
        LocalDate dateTime = LocalDate.now().minusDays(10);
        notificationService.deleteByDateTime(dateTime);
    }
}
