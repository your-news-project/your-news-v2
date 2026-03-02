package kr.co.yournews.apis.crawling.strategy.menu;

import kr.co.yournews.apis.crawling.strategy.dto.MenuFileDownloadReq;
import kr.co.yournews.domain.menu.type.MenuType;
import kr.co.yournews.domain.processedurl.service.ProcessedUrlService;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static kr.co.yournews.infra.redis.util.RedisConstants.DEFAULT_URL_TTL_SECONDS;

@Component
@RequiredArgsConstructor
public class MainCafeteriaMenuStrategy implements MenuFileStrategy {
    private final ProcessedUrlService processedUrlService;

    private static final String URL =
            "https://www.yu.ac.kr/main/life/cafeteria-menu.do?mode=list&article.offset=0";

    @Override
    public MenuType getMenuName() {
        return MenuType.MAIN_CAFETERIA;
    }

    @Override
    public String getScheduledTime() {
        return "0 30 10 ? * *";
    }

    @Override
    public String getUrl() {
        return URL;
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
    public String extractPostUrl(Element postElement) {
        Element titleElement = postElement.selectFirst("div.b-title-box > a");
        return titleElement != null ? titleElement.absUrl("href") : "";
    }

    @Override
    public boolean isProcessed(String postUrl) {
        return processedUrlService.existsByUrl(postUrl);
    }

    @Override
    public void markProcessed(String postUrl) {
        processedUrlService.save(postUrl, DEFAULT_URL_TTL_SECONDS);
    }

    @Override
    public boolean useDetailPage() {
        return false;
    }

    @Override
    public List<MenuFileDownloadReq> extractFiles(
            String postUrl,
            Element postEl,
            Document viewDoc
    ) {
        Elements elements = postEl.select(".b-m-common-file-box li");
        if (elements.isEmpty()) {
            return List.of();
        }

        List<MenuFileDownloadReq> files = new ArrayList<>();

        for (Element element : elements) {
            MenuFileDownloadReq req = toPdfDownloadReq(element);
            if (req != null) {
                files.add(req);
            }
        }

        return files;
    }

    /**
     * PDF 링크를 추출해 DTO로 변환
     */
    private MenuFileDownloadReq toPdfDownloadReq(
            Element element
    ) {
        Element textLink = element.selectFirst("a:not(.b-file-dwn):not(.b-file-preview)");
        if (textLink == null) return null;

        String fileName = textLink.text().trim();
        String fileUrl = textLink.absUrl("href");

        if (fileName.isEmpty() || fileUrl.isEmpty()) {
            return null;
        }

        return MenuFileDownloadReq.builder()
                .originalFileName(fileName)
                .fileUrl(fileUrl)
                .refererUrl(null)          // PDF는 referrer 필요 없음
                .menuType(getMenuName())
                .build();
    }
}
