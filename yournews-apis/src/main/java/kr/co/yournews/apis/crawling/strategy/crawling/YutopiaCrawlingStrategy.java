package kr.co.yournews.apis.crawling.strategy.crawling;

import kr.co.yournews.domain.processedurl.service.ProcessedUrlService;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static kr.co.yournews.infra.redis.util.RedisConstants.YUTOPIA_URL_TTL_SECONDS;

@Component
@RequiredArgsConstructor
public class YutopiaCrawlingStrategy implements CrawlingStrategy {
    private final ProcessedUrlService processedUrlService;
    private final UserService userService;

    private static final String NEWS_NAME = "YuTopia(비교과)";

    @Override
    public String getScheduledTime() {
        return "0 10 10 * * *";
    }

    @Override
    public boolean canHandle(String newsName) {
        return NEWS_NAME.equals(newsName);
    }

    @Override
    public Elements getPostElements(Document doc) {
        Elements postElements = doc.select("div[data-module='eco'][data-role='item'].OPEN");
        postElements.addAll(doc.select("div[data-module='eco'][data-role='item'].APPROACH_CLOSING"));
        return postElements;
    }

    @Override
    public boolean shouldProcessElement(Element postElement) {
        return true;
    }

    @Override
    public String extractPostTitle(Element postElement) {
        return postElement.selectFirst("div.title_wrap > b.title").text();
    }

    @Override
    public String extractPostUrl(Element postElement) {
        return postElement.selectFirst("a").absUrl("href");
    }

    @Override
    public List<Long> getSubscribedUsers(String newsName) {
        return userService.readAllUserIdsByNewsNameAndSubStatusTrue(newsName);
    }

    @Override
    public void saveUrl(String postUrl) {
        processedUrlService.save(postUrl, YUTOPIA_URL_TTL_SECONDS);
    }

    @Override
    public boolean isExisted(String postUrl) {
        return processedUrlService.existsByUrl(postUrl);
    }

    /**
     * 유토피아(비교과) 게시판의 여러 하위 카테고리에 해당하는 목록 URL들을 생성
     *
     * @param url : 유토피아 뉴스 정보 (기준이 되는 base url)
     * @return 크롤링 대상이 되는 유토피아 카테고리별 URL 목록
     */
    public List<String> getUrlsForYuTopiaNews(String url) {
        List<String> urls = new ArrayList<>();
        urls.add(url + 1 + "/list/0/1?sort=date");
        urls.add(url + 3 + "/list/0/1?sort=date");
        urls.add(url + 5 + "/list/0/1?sort=date");
        urls.add(url + 7 + "/list/0/1?sort=date");

        return urls;
    }
}
