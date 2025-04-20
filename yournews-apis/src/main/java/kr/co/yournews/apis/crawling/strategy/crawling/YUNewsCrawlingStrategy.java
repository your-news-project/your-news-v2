package kr.co.yournews.apis.crawling.strategy.crawling;

import kr.co.yournews.domain.processedurl.service.ProcessedUrlService;
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

    private static final String NEWS_NAME = "영대소식";

    @Override
    public String getScheduledTime() {
        return "0 0 8-19 * * MON-FRI";  // 주말 제외, 평일에 1시간마다 크롤링
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
    public List<String> getSubscribedUsers(String newsName) {
        return List.of(); // TODO : User에 구독 상황 넣고 추가
    }

    @Override
    public void saveUrl(String postURL) {
        processedUrlService.save(postURL, DEFAULT_URL_TTL_SECONDS);
    }

    @Override
    public boolean isExisted(String postURL) {
        return processedUrlService.existsByUrl(postURL);
    }

    // TODO : GPT 로직 구현
}
