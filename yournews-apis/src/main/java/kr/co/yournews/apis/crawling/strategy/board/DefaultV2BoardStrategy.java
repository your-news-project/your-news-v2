package kr.co.yournews.apis.crawling.strategy.board;

import kr.co.yournews.domain.processedurl.service.ProcessedUrlService;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static kr.co.yournews.infra.redis.util.RedisConstants.DEFAULT_URL_TTL_SECONDS;

@Component
@RequiredArgsConstructor
public class DefaultV2BoardStrategy implements BoardStrategy {
    private final ProcessedUrlService processedUrlService;
    private final UserService userService;

    private static final Set<String> NEWS_NAME = Set.of("반도체특성화대학", "AI/SW트랙");

    @Override
    public String getScheduledTime() {
        return "0 0 8-19 * * MON-FRI";
    }

    @Override
    public boolean canHandle(String newsName) {
        return NEWS_NAME.contains(newsName);
    }

    @Override
    public Elements getPostElements(Document doc) {
        return doc.select("tr");
    }

    @Override
    public boolean shouldProcessElement(Element postElement) {
        Element newPostElement = postElement.selectFirst("span.new");
        return newPostElement != null;
    }

    @Override
    public String extractPostTitle(Element postElement) {
        Element titleElement = postElement.selectFirst("td.subject.tal > a");
        return titleElement.text();
    }

    @Override
    public String extractPostUrl(Element postElement) {
        Element titleElement = postElement.selectFirst("td.subject.tal > a");
        return titleElement.absUrl("href");
    }

    @Override
    public List<Long> getSubscribedUsers(String newsName) {
        return userService.readAllUserIdsByNewsNameAndSubStatusTrue(newsName);
    }

    @Override
    public void saveUrl(String postUrl) {
        processedUrlService.save(postUrl, DEFAULT_URL_TTL_SECONDS);
    }

    @Override
    public boolean isExisted(String postUrl) {
        return processedUrlService.existsByUrl(postUrl);
    }
}
