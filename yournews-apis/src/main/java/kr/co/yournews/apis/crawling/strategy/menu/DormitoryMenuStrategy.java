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
public class DormitoryMenuStrategy implements MenuFileStrategy {
    private final ProcessedUrlService processedUrlService;

    private static final String URL =
            "https://www.yu.ac.kr/dormi/community/menu.do?mode=list&article.offset=0";

    @Override
    public MenuType getMenuName() {
        return MenuType.DORMITORY_MENU;
    }

    @Override
    public String getScheduledTime() {
        return "0 0 20 ? * SUN,MON,TUE,WED";
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
        return true;
    }

    @Override
    public List<MenuFileDownloadReq> extractFiles(
            String postUrl,
            Element postEl,
            Document viewDoc
    ) {
        if (viewDoc == null) {
            return List.of();
        }

        // 상세 본문 이미지들
        Elements imgs = viewDoc.select(".b-content-box .fr-view img");
        if (imgs.isEmpty()) {
            return List.of();
        }

        List<MenuFileDownloadReq> result = new ArrayList<>();

        for (Element img : imgs) {
            MenuFileDownloadReq req = toImageDownloadReq(postUrl, img);
            if (req != null) {
                result.add(req);
            }
        }

        return result;
    }

    /**
     * 상세 페이지의 <img> 하나를 다운로드 요청 DTO로 변환
     */
    private MenuFileDownloadReq toImageDownloadReq(
            String viewUrl,
            Element img
    ) {
        String imgUrl = img.absUrl("src");
        if (imgUrl.isBlank()) {
            return null;
        }

        // data-file_name 우선, 없으면 URL에서 파일명 추출
        String fileName = img.attr("data-file_name");
        if (fileName.isBlank()) {
            int idx = imgUrl.lastIndexOf('/');
            fileName = (idx != -1) ? imgUrl.substring(idx + 1) : "image.png";
        }

        return MenuFileDownloadReq.builder()
                .originalFileName(fileName)
                .fileUrl(imgUrl)
                .refererUrl(viewUrl)       // 이미지 다운로드 시 referrer 필요
                .menuType(getMenuName())
                .build();
    }
}
