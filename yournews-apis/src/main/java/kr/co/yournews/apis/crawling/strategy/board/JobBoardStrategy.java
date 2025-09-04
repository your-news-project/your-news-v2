package kr.co.yournews.apis.crawling.strategy.board;

import kr.co.yournews.common.util.DateTimeFormatterUtil;
import kr.co.yournews.domain.processedurl.service.ProcessedUrlService;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JobBoardStrategy implements BoardStrategy {
    private final ProcessedUrlService processedUrlService;
    private final UserService userService;

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

        return "회사 : " + companyElement.text() + "\n직종 : " + jobElement.text();
    }

    @Override
    public String extractPostUrl(Element postElement) {
        Element titleElement = postElement.selectFirst("td:nth-child(2) a");

        String postUrl = titleElement.absUrl("href");
        long ttl = calculateTTL(extractDeadline(postElement));
        deadlineCache.put(postUrl, ttl);

        return postUrl;
    }

    private String extractDeadline(Element postElement) {
        Element deadlineElement = postElement.selectFirst("td:nth-child(4)");
        return deadlineElement.text().split(" ")[1];
    }

    private long calculateTTL(String deadLine) {
        LocalDate deadlineDate = DateTimeFormatterUtil.parseToLocalDateTime(deadLine);
        LocalDate currentDate = LocalDate.now();

        long daysUntilDeadline = ChronoUnit.DAYS.between(currentDate, deadlineDate);

        return (daysUntilDeadline + 1) * 86400;
    }

    @Override
    public List<Long> getSubscribedUsers(String newsName) {
        return userService.readAllUserIdsByNewsNameAndSubStatusTrue(newsName);
    }

    @Override
    public void saveUrl(String postUrl) {
        processedUrlService.save(postUrl, deadlineCache.get(postUrl));
        deadlineCache.remove(postUrl);
    }

    @Override
    public boolean isExisted(String postUrl) {
        return processedUrlService.existsByUrl(postUrl);
    }
}
