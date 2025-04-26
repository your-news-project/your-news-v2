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

    @Override
    public boolean supports(CrawlingStrategy strategy) {
        return strategy instanceof YUNewsCrawlingStrategy;
    }

    @Override
    public void process(String newsName, Elements elements, CrawlingStrategy strategy) {
        YUNewsCrawlingStrategy yuNewsStrategy = (YUNewsCrawlingStrategy) strategy;

        Map<KeywordType, CrawlingPostInfo> keywordToPosts = extractNewPostsByKeyword(elements, yuNewsStrategy);
        if (keywordToPosts.isEmpty()) return;

        List<Long> userIds = yuNewsStrategy.getSubscribedUsers(newsName);
        if (userIds.isEmpty()) return;

        Map<Long, List<KeywordType>> userToKeywords = getUserToKeywords(userIds);

        String publicId = UUID.randomUUID().toString();
        List<Notification> notifications = new ArrayList<>();

        for (Long userId : userIds) {
            List<KeywordType> subscribedKeywords = userToKeywords.getOrDefault(userId, List.of());

            List<String> titles = new ArrayList<>();
            List<String> urls = new ArrayList<>();

            for (KeywordType keyword : subscribedKeywords) {
                CrawlingPostInfo info = keywordToPosts.get(keyword);
                if (info != null) {
                    titles.addAll(info.titles());
                    urls.addAll(info.urls());
                }
            }

            if (!titles.isEmpty()) {
                notifications.add(buildNotification(newsName, titles, urls, publicId, userId));
            }
        }

        if (!notifications.isEmpty()) {
            notificationCommandService.createNotifications(notifications);
        }

        List<FcmToken> tokens = fcmTokenService.readAllByUserIds(userIds);
        sendFcmMessages(tokens, newsName, publicId);
    }

    private Map<KeywordType, CrawlingPostInfo> extractNewPostsByKeyword(Elements elements, YUNewsCrawlingStrategy strategy) {
        Map<KeywordType, CrawlingPostInfo> keywordToPosts = new HashMap<>();

        for (Element element : elements) {
            if (!strategy.shouldProcessElement(element)) continue;

            String postTitle = strategy.extractPostTitle(element);
            String postUrl = strategy.extractPostUrl(element);


            KeywordType keyword = strategy.getKeyword(postTitle);
            keywordToPosts.computeIfAbsent(keyword, k -> new CrawlingPostInfo(new ArrayList<>(), new ArrayList<>()));

            CrawlingPostInfo postInfo = keywordToPosts.get(keyword);
            postInfo.titles().add(postTitle);
            postInfo.urls().add(postUrl);

            strategy.saveUrl(postUrl);
        }

        return keywordToPosts;
    }

    private Map<Long, List<KeywordType>> getUserToKeywords(List<Long> userIds) {
        return subNewsService.readUserKeywordsByUserIds(userIds).stream()
                .collect(Collectors.groupingBy(
                        UserKeywordDto::userId,
                        Collectors.mapping(UserKeywordDto::keywordType, Collectors.toList())
                ));
    }
}
