package kr.co.yournews.apis.notification.service;

import kr.co.yournews.apis.notification.dto.NotificationDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.entity.SubNews;
import kr.co.yournews.domain.news.service.SubNewsService;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.exception.NotificationErrorType;
import kr.co.yournews.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {
    private final NotificationService notificationService;
    private final SubNewsService subNewsService;

    /**
     * 특정 알림 조회 메서드 - 알림의 pk 값을 통해 읽음
     * - 최초 조회 시, 읽음 처리 수행
     *
     * @param notificationId : 조회할 알림의 pk
     * @return : 알림 상세 정보 DTO
     * @throws CustomException NOT_FOUND : 알림가 존재하지 않는 경우
     */
    @Transactional
    public NotificationDto.Details getNotificationById(Long notificationId) {
        Notification notification = notificationService.readById(notificationId)
                .orElseThrow(() -> new CustomException(NotificationErrorType.NOT_FOUND));

        if (!notification.isRead()) {
            notification.markAsRead();
        }

        return NotificationDto.Details.from(notification);
    }

    /**
     * 특정 알림 조회 메서드 - 알림의 사용자 id & public id를 통해 읽음 (푸쉬 알림을 통해 바로 이동을 위한 메서드)
     * - 최초 조회 시, 읽음 처리 수행
     *
     * @param userId   : 사용자 id
     * @param publicId : 알림의 public id (UUID 형식)
     * @return : 알림 상세 정보 DTO
     * @throws CustomException NOT_FOUND : 알림가 존재하지 않는 경우
     */
    @Transactional
    public NotificationDto.Details getNotificationByPublicId(Long userId, String publicId) {
        Notification notification = notificationService.readByUserIdAndPublicId(userId, publicId)
                .orElseThrow(() -> new CustomException(NotificationErrorType.NOT_FOUND));

        if (!notification.isRead()) {
            notification.markAsRead();
        }

        return NotificationDto.Details.from(notification);
    }

    /**
     * 사용자 ID 및 읽음 여부를 기준으로 알림 목록을 페이징하여 조회
     *
     * @param userId   : 사용자 ID
     * @param isRead   : 읽음 여부 필터
     * @param pageable : 페이징 및 정렬 정보
     * @return : 알림 요약 정보가 담긴 Page 객체
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto.Summary> getNotificationsByUserIdAndIsRead(
            Long userId, boolean isRead, Pageable pageable
    ) {
        return notificationService.readAllByUserIdAndIsRead(userId, isRead, pageable)
                .map(NotificationDto.Summary::from);
    }

    /**
     * 사용자 ID 및 소식 이름, 읽음 여부를 기준으로 알림 목록을 페이징하여 조회
     *
     * @param userId   : 사용자 ID
     * @param newsName :
     * @param isRead   : 읽음 여부 필터
     * @param pageable : 페이징 및 정렬 정보
     * @return : 알림 요약 정보가 담긴 Page 객체
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto.Summary> getNotificationsByUserIdAndNewsNameAndIsRead(
            Long userId,
            String newsName,
            boolean isRead,
            Pageable pageable
    ) {
        return notificationService.readAllByUserIdAndNewsNameAndIsRead(userId, newsName, isRead, pageable)
                .map(NotificationDto.Summary::from);
    }

    /**
     * 사용자 ID 및 소식 이름, 읽음 여부를 기준으로 알림 목록을 페이징하여 조회
     *
     * @param userId   : 사용자 ID
     * @param isRead   : 읽음 여부 필터
     * @param pageable : 페이징 및 정렬 정보
     * @return : 알림 요약 정보가 담긴 Page 객체
     */
    @Transactional(readOnly = true)
    public Page<NotificationDto.Summary> getNotificationsByUserIdAndNewsNameNotInAndIsRead(
            Long userId,
            boolean isRead,
            Pageable pageable
    ) {
        List<String> subscription = subNewsService.readByUserId(userId)
                .stream()
                .map(SubNews::getNewsName)
                .toList();

        if (subscription.isEmpty()) {
            return notificationService.readAllByUserIdAndIsRead(userId, isRead, pageable)
                    .map(NotificationDto.Summary::from);
        }

        return notificationService.readByUserIdAndNewsNameNotInAndIsRead(userId, subscription, isRead, pageable)
                .map(NotificationDto.Summary::from);
    }

    /**
     * 북마크 표시된 알림 조회 메서드
     *
     * @param userId : 사용자 ID
     * @return : 북마크 표시된 알림 리스트
     */
    @Transactional(readOnly = true)
    public List<NotificationDto.Summary> getNotificationsByUserIdAndIsBookmarkedTrue(
            Long userId
    ) {
        return notificationService.readAllByUserIdAndIsBookmarkedTrue(userId)
                .stream().map(NotificationDto.Summary::from).toList();
    }

    /**
     * 읽지 않은 알림 개수 조회 메서드
     *
     * @param userId : 사용자 ID
     * @return : 읽지 않은 알림 개수
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationService.readUnreadCountByUserId(userId);
    }
}
