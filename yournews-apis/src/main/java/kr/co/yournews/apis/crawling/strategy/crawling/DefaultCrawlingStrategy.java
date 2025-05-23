package kr.co.yournews.apis.crawling.strategy.crawling;

import kr.co.yournews.domain.processedurl.service.ProcessedUrlService;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;

import static kr.co.yournews.infra.redis.util.RedisConstants.DEFAULT_URL_TTL_SECONDS;

@Component
@RequiredArgsConstructor
public class DefaultCrawlingStrategy implements CrawlingStrategy {
    private final ProcessedUrlService processedUrlService;
    private final UserService userService;

    private static final List<String> EXCLUDED_NEWS_NAME =
            List.of("YuTopia(비교과)", "영대소식", "반도체특성화대학", "AI/SW트랙", "취업처");

    @Override
    public String getScheduledTime() {
        return "0 0 8-19 * * MON-FRI";
    }

    @Override
    public boolean canHandle(String newsName) {
        return !EXCLUDED_NEWS_NAME.contains(newsName);
    }

    @Override
    public Elements getPostElements(Document doc) {
        Elements postElements = doc.select("tr[class='']");
        postElements.addAll(doc.select("tr.b-top-box"));
        return postElements;
    }

    @Override
    public boolean shouldProcessElement(Element postElement) {
        Element newPostElement = postElement.selectFirst("p.b-new");
        return newPostElement != null;
    }

    @Override
    public String extractPostTitle(Element postElement) {
        Element titleElement = postElement.selectFirst("div.b-title-box > a");
        return titleElement.text();
    }

    @Override
    public String extractPostUrl(Element postElement) {
        Element titleElement = postElement.selectFirst("div.b-title-box > a");
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
