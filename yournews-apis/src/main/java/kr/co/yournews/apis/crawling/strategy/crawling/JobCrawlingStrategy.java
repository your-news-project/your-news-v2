package kr.co.yournews.apis.crawling.strategy.crawling;

import kr.co.yournews.domain.processedurl.service.ProcessedUrlService;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JobCrawlingStrategy implements CrawlingStrategy {
    private final ProcessedUrlService processedUrlService;

    private final Map<String, Long> deadlineCache = new HashMap<>();

    private static final String JOB_BOARD_NAME = "취업처";

    @Override
    public String getScheduledTime() {
        return "0 10 10 * * *";
    }

    @Override
    public boolean canHandle(String newsName) {
        return JOB_BOARD_NAME.equals(newsName);
    }

    @Override
    public Elements getPostElements(Document doc) {
        return doc.select("tr");
    }

    @Override
    public boolean shouldProcessElement(Element postElement) {
        Element newPostElement = postElement.selectFirst("td:nth-child(5) img[alt='모집중']");
        return newPostElement != null;
    }

    @Override
    public String extractPostTitle(Element postElement) {
        Element companyElement = postElement.selectFirst("td:nth-child(1) a");
        Element jobElement = postElement.selectFirst("td:nth-child(2) a");

        return "회사 : " + companyElement.text() + "<br>직종 : " + jobElement.text();
    }

    @Override
    public String extractPostUrl(Element postElement) {
        Element titleElement = postElement.selectFirst("td:nth-child(2) a");

        String postURL = titleElement.absUrl("href");
        long ttl = calculateTTL(extractDeadline(postElement));
        deadlineCache.put(postURL, ttl);

        return postURL;
    }

    private String extractDeadline(Element postElement) {
        Element deadlineElement = postElement.selectFirst("td:nth-child(4)");
        return deadlineElement.text().split(" ")[1];
    }

    private long calculateTTL(String deadLine) {
        return 0; // TODO : 게시글 ttl 설정
    }

    @Override
    public List<String> getSubscribedUsers(String newsName) {
        return List.of(); // TODO : User에 구독 상황 넣고 추가
    }

    @Override
    public void saveUrl(String postURL) {
        processedUrlService.save(postURL, deadlineCache.get(postURL));
        deadlineCache.remove(postURL);
    }

    @Override
    public boolean isExisted(String postURL) {
        return processedUrlService.existsByUrl(postURL);
    }
}
