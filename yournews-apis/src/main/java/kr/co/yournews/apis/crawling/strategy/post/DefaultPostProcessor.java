package kr.co.yournews.apis.crawling.strategy.post;

import kr.co.yournews.apis.crawling.strategy.crawling.CrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.crawling.YUNewsCrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.dto.CrawlingPostInfo;
import kr.co.yournews.apis.notification.service.NotificationCommandService;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.infra.rabbitmq.RabbitMessagePublisher;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DefaultPostProcessor extends PostProcessor {
    private final NotificationCommandService notificationCommandService;
    private final FcmTokenService fcmTokenService;

    public DefaultPostProcessor(NotificationCommandService notificationCommandService,
                                FcmTokenService fcmTokenService,
                                RabbitMessagePublisher rabbitMessagePublisher) {
        super(rabbitMessagePublisher);
        this.notificationCommandService = notificationCommandService;
        this.fcmTokenService = fcmTokenService;
    }

    @Override
    public boolean supports(CrawlingStrategy strategy) {
        return !(strategy instanceof YUNewsCrawlingStrategy);
    }

    @Override
    public void process(String newsName, Elements elements, CrawlingStrategy strategy) {
        CrawlingPostInfo postInfo = extractNewPosts(elements, strategy);
        if (postInfo.isEmpty()) return;

        String publicId = UUID.randomUUID().toString();

        List<Long> userIds = strategy.getSubscribedUsers(newsName);
        if (userIds.isEmpty()) return;

        createAndSaveNotifications(userIds, newsName, postInfo.titles(), postInfo.urls(), publicId);

        List<FcmToken> tokens = fcmTokenService.readAllByUserIds(userIds);
        sendFcmMessages(tokens, newsName, publicId);
    }

    private CrawlingPostInfo extractNewPosts(Elements elements, CrawlingStrategy strategy) {
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

    private void createAndSaveNotifications(List<Long> userIds, String newsName,
                                            List<String> titles, List<String> urls,
                                            String publicId) {
        List<Notification> notifications = userIds.stream()
                .map(userId -> buildNotification(newsName, titles, urls, publicId, userId))
                .toList();

        notificationCommandService.createNotifications(notifications);
    }
}
