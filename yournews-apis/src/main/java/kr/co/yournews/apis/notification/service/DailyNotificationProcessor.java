package kr.co.yournews.apis.notification.service;

import kr.co.yournews.apis.notification.dto.FcmMessageDto;
import kr.co.yournews.apis.notification.dto.DailyNewsDto;
import kr.co.yournews.domain.news.entity.News;
import kr.co.yournews.domain.news.service.NewsService;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.type.NotificationType;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.apis.notification.constant.NotificationConstant;
import kr.co.yournews.infra.rabbitmq.RabbitMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyNotificationProcessor {
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final DailyNotificationService dailyNotificationService;
    private final NewsService newsService;
    private final UserService userService;
    private final FcmTokenService fcmTokenService;
    private final NotificationCommandService notificationCommandService;

    /**
     * 일간 소식을 전송하는 메서드
     */
    public void sendDailyNotification() {
        newsService.readAll().stream()
                .map(News::getName)
                .forEach(this::processNews);
    }

    /**
     * 소식에 대한 일간 알림 처리 메서드
     * - Redis에서 해당 소식의 게시글 목록(DailyNewsDto)을 조회
     * - 사용자 ID 및 FCM 토큰을 조회
     * - 알림 데이터를 생성하여 저장
     * - FCM 메시지를 발송
     *
     * @param newsName : 처리할 소식 이름
     */
    private void processNews(String newsName) {
        List<DailyNewsDto> newsListDtos = dailyNotificationService.getAllNewsInfo(newsName);
        if (newsListDtos.isEmpty()) return;

        log.info("[소식 처리 시작] 소식명: {}", newsName);

        String publicId = UUID.randomUUID().toString();
        List<Long> userIds = userService.readAllUserIdsByNewsNameAndDailySubStatusTrue(newsName);
        List<FcmToken> tokens = fcmTokenService.readAllByUserIds(userIds);

        saveNotifications(userIds, newsName, newsListDtos, publicId);
        sendFcmMessages(tokens, newsName, publicId);

        log.info("[소식 처리 완료] 소식명: {}, 사용자 수: {}, 토큰 수: {}", newsName, userIds.size(), tokens.size());
    }

    /**
     * 사용자별 Notification 생성 및 저장
     *
     * @param userIds      : 알림을 보낼 사용자 ID 리스트
     * @param newsName     : 소식(뉴스) 이름
     * @param newsListDtos : 게시글 제목, url 리스트
     * @param publicId     : 모든 알림에 공통으로 사용할 Public ID (묶음 식별용)
     */
    private void saveNotifications(List<Long> userIds, String newsName,
                                   List<DailyNewsDto> newsListDtos,
                                   String publicId) {

        List<String> titles = new ArrayList<>();
        List<String> urls = new ArrayList<>();

        for (DailyNewsDto dto : newsListDtos) {
            titles.add(dto.title());
            urls.add(dto.url());
        }

        List<Notification> notifications = userIds.stream()
                .map(userId -> buildNotification(newsName, titles, urls, publicId, userId))
                .toList();

        notificationCommandService.createNotifications(notifications);

        log.info("[알림 저장 완료] 소식명: {}, 사용자 수: {}, 게시글 수: {}, publicId: {}",
                newsName, userIds.size(), titles.size(), publicId);
    }

    /**
     * Notification 엔티티 생성
     */
    private Notification buildNotification(String newsName,
                                           List<String> titles,
                                           List<String> urls,
                                           String publicId,
                                           Long userId) {

        return Notification.builder()
                .newsName(newsName)
                .postTitle(titles)
                .postUrl(urls)
                .publicId(publicId)
                .type(NotificationType.DAILY)
                .isRead(false)
                .userId(userId)
                .build();
    }

    /**
     * FCM 메시지 전송 (RabbitMQ 이용)
     */
    private void sendFcmMessages(List<FcmToken> tokens, String newsName, String publicId) {
        String title = NotificationConstant.getDailyNewsNotificationTitle(newsName);

        log.info("[알림 메시지 큐 전송 시작] 소식명: {}, 토큰 수: {}", newsName, tokens.size());

        for (int idx = 0; idx < tokens.size(); idx++) {
            FcmToken token = tokens.get(idx);
            boolean isFirst = (idx == 0); // 첫번째 토큰 여부 판단
            boolean isLast = (idx == tokens.size() - 1); // 마지막 토큰 여부 판단
            rabbitMessagePublisher.send(
                    FcmMessageDto.of(token.getToken(), title, publicId, isFirst, isLast)
            );
        }

        log.info("[알림 메시지 큐 전송 완료] 소식명: {}, publicId: {}", newsName, publicId);
    }
}
