package kr.co.yournews.apis.crawling.strategy.post;

import kr.co.yournews.apis.crawling.strategy.crawling.CrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.crawling.YUNewsCrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.dto.CrawlingPostInfo;
import kr.co.yournews.apis.notification.service.DailyNotificationService;
import kr.co.yournews.apis.notification.service.NotificationCommandService;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.infra.rabbitmq.RabbitMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class DefaultPostProcessor extends PostProcessor {
    private final NotificationCommandService notificationCommandService;
    private final FcmTokenService fcmTokenService;
    private final DailyNotificationService dailyNotificationService;

    public DefaultPostProcessor(
            NotificationCommandService notificationCommandService,
            FcmTokenService fcmTokenService,
            RabbitMessagePublisher rabbitMessagePublisher,
            DailyNotificationService dailyNotificationService
    ) {
        super(rabbitMessagePublisher);
        this.notificationCommandService = notificationCommandService;
        this.fcmTokenService = fcmTokenService;
        this.dailyNotificationService = dailyNotificationService;
    }

    /**
     * 이 프로세서가 지원하는 전략인지 여부
     * - 기본 프로세서는 YUNewsCrawlingStrategy를 제외한 나머지를 처리
     *
     * @param strategy : 적용할 크롤링 전략
     * @return : YUNewsCrawlingStrategy일 경우 false, 그 외 true
     */
    @Override
    public boolean supports(CrawlingStrategy strategy) {
        return !(strategy instanceof YUNewsCrawlingStrategy);
    }

    /**
     * 주어진 게시글 요소(elements)를 처리하는 메서드
     * - 새 게시글을 필터링
     * - 일간 소식 정보 저장
     * - 사용자별 Notification 저장
     * - FCM 알림 발송
     *
     * @param newsName : 등록된 소식 이름
     * @param elements : 크롤링된 HTML 구조
     * @param strategy : 크롤링 처리 전략
     */
    @Override
    public void process(String newsName, Elements elements, CrawlingStrategy strategy) {
        log.info("[크롤링 처리 시작] newsName: {}, strategy: {}", newsName, strategy.getClass().getSimpleName());

        CrawlingPostInfo postInfo = extractNewPosts(elements, strategy);
        if (postInfo.isEmpty()) {
            log.info("[새 게시글 없음] newsName: {}", newsName);
            return;
        }

        // 일간 소식 정보 저장
        dailyNotificationService.saveNewsInfo(newsName, postInfo.titles(), postInfo.urls());

        List<Long> userIds = strategy.getSubscribedUsers(newsName);
        if (userIds.isEmpty()) {
            log.info("[구독자 없음] newsName: {}", newsName);
            return;
        }

        // 모든 알림에 공통으로 사용될 public_id (알림 페이지 이동을 위해)
        String publicId = UUID.randomUUID().toString();
        saveNotifications(userIds, newsName, postInfo.titles(), postInfo.urls(), publicId);
        log.info("[알림 저장 완료] 사용자 수: {}, newsName: {}, publicId: {}", userIds.size(), newsName, publicId);

        List<FcmToken> tokens = fcmTokenService.readAllByUserIds(userIds);
        sendFcmMessages(tokens, newsName, publicId);
    }

    /**
     * Elements에서 새 게시글만 추출
     * - 이미 존재하는 게시글은 제외
     * - 크롤링 전략이 정의한 기준에 맞는 게시글만 추출
     *
     * @param elements : 크롤링된 HTML 구조
     * @param strategy : 크롤링 처리 전략
     * @return : 분류된 새 게시글 정보 dto (titles, urls)
     */
    private CrawlingPostInfo extractNewPosts(
            Elements elements,
            CrawlingStrategy strategy
    ) {
        List<String> titles = new ArrayList<>();
        List<String> urls = new ArrayList<>();

        for (Element element : elements) {
            if (!strategy.shouldProcessElement(element)) continue;

            String postTitle = strategy.extractPostTitle(element);
            String postUrl = strategy.extractPostUrl(element);

            if (strategy.isExisted(postUrl)) continue;

            titles.add(postTitle);
            urls.add(postUrl);
            strategy.saveUrl(postUrl);
        }

        return new CrawlingPostInfo(titles, urls);
    }

    /**
     * 사용자별 Notification 생성 및 저장
     *
     * @param userIds  : 알림을 보낼 사용자 ID 리스트
     * @param newsName : 소식(뉴스) 이름
     * @param titles   : 게시글 제목 리스트
     * @param urls     : 게시글 URL 리스트
     * @param publicId : 모든 알림에 공통으로 사용할 Public ID (묶음 식별용)
     */
    private void saveNotifications(
            List<Long> userIds, String newsName,
            List<String> titles,
            List<String> urls,
            String publicId
    ) {
        List<Notification> notifications = userIds.stream()
                .map(userId -> buildNotification(newsName, titles, urls, publicId, userId))
                .toList();

        notificationCommandService.createNotifications(notifications);
    }
}
