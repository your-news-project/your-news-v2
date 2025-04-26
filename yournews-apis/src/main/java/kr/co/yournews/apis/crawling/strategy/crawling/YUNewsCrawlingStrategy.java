package kr.co.yournews.apis.crawling.strategy.crawling;

import kr.co.yournews.domain.news.type.KeywordType;
import kr.co.yournews.domain.processedurl.service.ProcessedUrlService;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.infra.openai.KeywordClassificationClient;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;

import static kr.co.yournews.infra.redis.util.RedisConstants.DEFAULT_URL_TTL_SECONDS;

@Component
@RequiredArgsConstructor
public class YUNewsCrawlingStrategy implements CrawlingStrategy {
    private final ProcessedUrlService processedUrlService;
    private final KeywordClassificationClient keywordClassificationClient;
    private final UserService userService;

    private static final String NEWS_NAME = "영대소식";

    @Override
    public String getScheduledTime() {
        return "0 0 8-19 * * MON-FRI";
    }

    @Override
    public boolean canHandle(String newsName) {
        return NEWS_NAME.equals(newsName);
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

    public KeywordType getKeyword(String postTitle) {
        String keyword = keywordClassificationClient.requestKeyword(postTitle);
        return KeywordType.fromLabel(keyword);
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
