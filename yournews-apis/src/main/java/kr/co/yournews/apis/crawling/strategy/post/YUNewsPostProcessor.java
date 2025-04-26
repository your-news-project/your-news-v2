package kr.co.yournews.apis.crawling.strategy.post;

import kr.co.yournews.apis.crawling.strategy.crawling.CrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.crawling.YUNewsCrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.dto.CrawlingPostInfo;
import kr.co.yournews.apis.notification.service.NotificationCommandService;
import kr.co.yournews.domain.news.dto.UserKeywordDto;
import kr.co.yournews.domain.news.service.SubNewsService;
import kr.co.yournews.domain.news.type.KeywordType;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.infra.rabbitmq.RabbitMessagePublisher;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class YUNewsPostProcessor extends PostProcessor {
    private final NotificationCommandService notificationCommandService;
    private final FcmTokenService fcmTokenService;
    private final SubNewsService subNewsService;

    public YUNewsPostProcessor(NotificationCommandService notificationCommandService,
                               FcmTokenService fcmTokenService,
                               SubNewsService subNewsService,
                               RabbitMessagePublisher rabbitMessagePublisher) {
        super(rabbitMessagePublisher);
        this.notificationCommandService = notificationCommandService;
        this.fcmTokenService = fcmTokenService;
        this.subNewsService = subNewsService;
    }

    /**
     * 해당 PostProcessor가 주어진 CrawlingStrategy를 지원하는지 여부 반환
     *
     * @param strategy 적용할 크롤링 전략
     * @return YUNewsCrawlingStrategy일 경우 true, 그 외 false
     */
    @Override
    public boolean supports(CrawlingStrategy strategy) {
        return strategy instanceof YUNewsCrawlingStrategy;
    }

    /**
     * 주어진 게시글 요소(elements)를 처리하는 메서드
     * - 키워드 기반으로 새 게시글을 분류
     * - 사용자별 Notification 저장
     * - FCM 알림 전송
     *
     * @param newsName : 등록된 소식 이름
     * @param elements : 크롤링된 HTML 구조
     * @param strategy : 크롤링 처리 전략
     */
    @Override
    public void process(String newsName, Elements elements, CrawlingStrategy strategy) {
        YUNewsCrawlingStrategy yuNewsStrategy = (YUNewsCrawlingStrategy) strategy;

        Map<KeywordType, CrawlingPostInfo> keywordToPosts = extractNewPostsByKeyword(elements, yuNewsStrategy);
        if (keywordToPosts.isEmpty()) return;

        List<Long> userIds = yuNewsStrategy.getSubscribedUsers(newsName);
        if (userIds.isEmpty()) return;

        Map<Long, List<KeywordType>> userToKeywords = getUserToKeywords(userIds);

        // 모든 알림에 공통으로 사용될 public_id (알림 페이지 이동을 위해)
        String publicId = UUID.randomUUID().toString();

        saveNotifications(userIds, userToKeywords, keywordToPosts, newsName, publicId);

        List<FcmToken> tokens = fcmTokenService.readAllByUserIds(userIds);
        sendFcmMessages(tokens, newsName, publicId);
    }

    /**
     * 크롤링된 Elements에서 새 게시글을 키워드 기준으로 분류
     *
     * @param elements : 크롤링된 HTML 구조
     * @param strategy : 크롤링 처리 전략
     * @return : 키워드별로 분류된 새 게시글 정보 Map
     */
    private Map<KeywordType, CrawlingPostInfo> extractNewPostsByKeyword(Elements elements, YUNewsCrawlingStrategy strategy) {
        Map<KeywordType, CrawlingPostInfo> keywordToPosts = new HashMap<>();

        for (Element element : elements) {
            if (!strategy.shouldProcessElement(element)) continue;

            String postTitle = strategy.extractPostTitle(element);
            String postUrl = strategy.extractPostUrl(element);

            if (strategy.isExisted(postUrl)) continue;

            // 제목을 통해 키워드 추출 (GPT 호출)
            KeywordType keyword = strategy.getKeyword(postTitle);
            keywordToPosts.computeIfAbsent(keyword, k -> new CrawlingPostInfo(new ArrayList<>(), new ArrayList<>()));

            // 존재하는 키워드에 게시글 추가
            CrawlingPostInfo postInfo = keywordToPosts.get(keyword);
            postInfo.titles().add(postTitle);
            postInfo.urls().add(postUrl);

            strategy.saveUrl(postUrl);
        }

        return keywordToPosts;
    }

    /**
     * 사용자별 구독 키워드를 조회
     *
     * @param userIds : 사용자 ID 리스트
     * @return : 사용자 ID별 구독 키워드 매핑
     */
    private Map<Long, List<KeywordType>> getUserToKeywords(List<Long> userIds) {
        return subNewsService.readUserKeywordsByUserIds(userIds).stream()
                .collect(Collectors.groupingBy(
                        UserKeywordDto::userId,
                        Collectors.mapping(UserKeywordDto::keywordType, Collectors.toList())
                ));
    }

    /**
     * 사용자별로 키워드 매칭된 게시글을 기반으로 Notification 생성 및 저장
     *
     * @param userIds        : 알림을 보낼 사용자 ID 리스트
     * @param userToKeywords : 사용자별 구독 키워드 매핑
     * @param keywordToPosts : 키워드별 크롤링된 게시글 매핑
     * @param newsName       : 소식(뉴스) 이름
     * @param publicId       : 모든 알림에 공통으로 사용할 Public ID (묶음 식별용)
     */
    private void saveNotifications(List<Long> userIds,
                                   Map<Long, List<KeywordType>> userToKeywords,
                                   Map<KeywordType, CrawlingPostInfo> keywordToPosts,
                                   String newsName,
                                   String publicId) {

        List<Notification> notifications = new ArrayList<>();

        for (Long userId : userIds) {
            List<KeywordType> keywords = userToKeywords.getOrDefault(userId, List.of());

            List<String> titles = new ArrayList<>();
            List<String> urls = new ArrayList<>();

            // 사용자가 구독한 키워드들의 새로운 소식만 알림에 추가
            for (KeywordType keyword : keywords) {
                CrawlingPostInfo postInfo = keywordToPosts.get(keyword);
                if (postInfo != null) {
                    titles.addAll(postInfo.titles());
                    urls.addAll(postInfo.urls());
                }
            }

            if (!titles.isEmpty()) {
                notifications.add(buildNotification(newsName, titles, urls, publicId, userId));
            }
        }

        if (!notifications.isEmpty()) {
            notificationCommandService.createNotifications(notifications);
        }
    }
}
