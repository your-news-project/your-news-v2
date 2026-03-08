package kr.co.yournews.apis.notification.service;

import kr.co.yournews.apis.notification.constant.NotificationConstant;
import kr.co.yournews.apis.notification.dto.DailyNewsDto;
import kr.co.yournews.common.util.FcmMessageFormatter;
import kr.co.yournews.domain.news.entity.News;
import kr.co.yournews.domain.news.service.NewsService;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.type.NotificationType;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.domain.user.service.UserService;
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
    private final DailyNotificationService dailyNotificationService;
    private final NewsService newsService;
    private final UserService userService;
    private final FcmTokenService fcmTokenService;
    private final NotificationDispatchService notificationDispatchService;

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
        if (newsListDtos.isEmpty()) {
            return;
        }

        log.info("[소식 처리 시작] 소식명: {}", newsName);

        String publicId = UUID.randomUUID().toString();
        List<Long> userIds = userService.readAllUserIdsByNewsNameAndDailySubStatusTrue(newsName);
        if (userIds.isEmpty()) {
            return;
        }

        List<FcmToken> tokens = fcmTokenService.readAllByUserIds(userIds);
        List<String> titles = extractTitles(newsListDtos);
        List<String> urls = extractUrls(newsListDtos);

        List<Notification> notifications = userIds.stream()
                .map(userId -> buildNotification(newsName, titles, urls, publicId, userId))
                .toList();

        String title = NotificationConstant.getDailyNewsNotificationTitle(newsName);
        String content = FcmMessageFormatter.formatTitles(titles);

        notificationDispatchService.saveNotificationsAndEnqueueOutbox(
                notifications,
                tokens,
                title,
                content,
                publicId
        );

        log.info("[일간 알림 저장 + 아웃박스 적재 완료] 소식명: {}, 사용자 수: {}, 토큰 수: {}",
                newsName, userIds.size(), tokens.size());
    }

    /**
     * 일간 소식 중 제목 추출 메서드
     *
     * @param newsListDtos : 일간 소식 목록 DTO
     * @return : 일간 소식 제목 추출 리스트
     */
    private List<String> extractTitles(List<DailyNewsDto> newsListDtos) {
        List<String> titles = new ArrayList<>();
        for (DailyNewsDto dto : newsListDtos) {
            titles.add(dto.title());
        }
        return titles;
    }

    /**
     * 일간 소식 중 url 추출 메서드
     *
     * @param newsListDtos : 일간 소식 목록 DTO
     * @return : 일간 소식 url 추출 리스트
     */
    private List<String> extractUrls(List<DailyNewsDto> newsListDtos) {
        List<String> urls = new ArrayList<>();
        for (DailyNewsDto dto : newsListDtos) {
            urls.add(dto.url());
        }
        return urls;
    }

    /**
     * Notification 엔티티 생성
     */
    private Notification buildNotification(
            String newsName,
            List<String> titles,
            List<String> urls,
            String publicId,
            Long userId
    ) {
        return Notification.builder()
                .newsName(newsName)
                .postTitle(titles)
                .postUrl(urls)
                .publicId(publicId)
                .type(NotificationType.DAILY)
                .isRead(false)
                .isBookmarked(false)
                .userId(userId)
                .build();
    }

}
