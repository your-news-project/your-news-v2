package kr.co.yournews.apis.crawling.service;

import kr.co.yournews.apis.crawling.strategy.dto.MenuFileDownloadReq;
import kr.co.yournews.apis.crawling.strategy.menu.MenuFileStrategy;
import kr.co.yournews.infra.crawling.CrawlingProcessor;
import kr.co.yournews.infra.menu.storage.MenuFileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuCrawlingExecutor {
    private final CrawlingProcessor crawlingProcessor;
    private final MenuFileStorage menuFileStorage;

    /**
     * 주어진 MenuFileStrategy 따라 크롤링을 실행하는 메서드
     * 크롤링 및 후처리를 수행함
     *
     * @param strategy : 크롤링 전략
     */
    public void executeStrategy(MenuFileStrategy strategy) {
        String url = strategy.getUrl();
        log.info("[학식 크롤링 시작] strategy: {}", strategy.getClass().getSimpleName());

        Document doc = crawlingProcessor.fetch(url);

        if (doc == null) {
            log.warn("[학식 크롤링 실패] Document null - menuName: {}, url: {}",
                    strategy.getMenuName(), url);
            return;
        }

        Elements elements = strategy.getPostElements(doc);

        if (elements.isEmpty()) {
            log.info("[학식 게시글 없음] menuName: {}, url: {}", strategy.getMenuName(), url);
            return;
        }

        processPosts(strategy, elements);

        log.info("[학식 크롤링 완료] menuName: {}", strategy.getMenuName());
    }

    /**
     * 게시글 목록(Element들)을 순회하며 각 게시글을 처리하는 메서드
     *
     * @param strategy 현재 실행 중인 크롤링 전략
     * @param elements 게시글 Element 리스트
     */
    private void processPosts(
            MenuFileStrategy strategy,
            Elements elements
    ) {
        for (Element element : elements) {
            // N 표시 필터링
            if (!strategy.shouldProcessElement(element)) {
                continue;
            }

            // 제목 / 상세 URL 추출
            String postUrl = strategy.extractPostUrl(element);
            if (postUrl == null || postUrl.isBlank()) {
                continue;
            }

            // 이미 처리된 게시글 스킵
            if (strategy.isProcessed(postUrl)) {
                log.info("[학식 크롤링 스킵] 이미 처리된 게시글 - menuName: {}, url: {}",
                        strategy.getMenuName(), postUrl);
                continue;
            }

            // 상세 페이지 HTML 필요 여부에 따라 fetch (DormitoryMenuStrategy)
            Document viewDoc = null;
            if (strategy.useDetailPage()) {
                viewDoc = crawlingProcessor.fetch(postUrl);
                if (viewDoc == null) {
                    log.warn("[학식 상세 크롤링 실패] menuName: {}, url: {}", strategy.getMenuName(), postUrl);
                    continue;
                }
            }

            // 파일(PDF/이미지) 목록 추출
            List<MenuFileDownloadReq> files =
                    strategy.extractFiles(postUrl, element, viewDoc);

            if (files == null || files.isEmpty()) {
                log.info("[학식 파일 없음] menuName: {}, url: {}", strategy.getMenuName(), postUrl);
                strategy.markProcessed(postUrl);
                continue;
            }

            saveFiles(files);
            strategy.markProcessed(postUrl);
        }
    }

    /**
     * PDF 및 이미지 파일 저장 요청 메서드
     *
     * @param files 메뉴 PDF 또는 이미지 다운로드 요청 목록
     */
    private void saveFiles(List<MenuFileDownloadReq> files) {
        for (MenuFileDownloadReq req : files) {
            menuFileStorage.downloadAndStore(
                    req.originalFileName(), req.fileUrl(),
                    req.refererUrl(), req.menuType()
            );
        }
    }
}
