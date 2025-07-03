package kr.co.yournews.apis.crawling.service;

import kr.co.yournews.apis.crawling.strategy.crawling.CrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.crawling.YutopiaCrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.post.PostProcessor;
import kr.co.yournews.domain.news.service.NewsService;
import kr.co.yournews.infra.crawling.NewsProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingExecutor {
    private final NewsService newsService;
    private final NewsProcessor newsProcessor;
    private final List<PostProcessor> postProcessors;

    /**
     * 주어진 CrawlingStrategy에 따라 크롤링을 실행하는 메서드
     * 각 뉴스에 대해 전략이 처리 가능한지 판단한 후, 크롤링 및 후처리를 수행한
     *
     * @param strategy : 크롤링 전략
     */
    @Async
    public void executeStrategy(CrawlingStrategy strategy) {
        String strategyName = strategy.getClass().getSimpleName();
        log.info("[크롤링 시작] strategy: {}", strategyName);

        newsService.readAll().stream()
                .filter(news -> strategy.canHandle(news.getName()))
                .forEach(news -> {
                    if (strategy instanceof YutopiaCrawlingStrategy yuStrategy) {
                        yuStrategy.getUrlsForYuTopiaNews(news.getUrl()).forEach(url ->
                                crawlAndProcess(news.getName(), url, strategy)
                        );
                    } else {
                        crawlAndProcess(news.getName(), news.getUrl(), strategy);
                    }
                });
        log.info("[크롤링 완료] strategy: {}", strategyName);
    }

    /**
     * 뉴스 이름과 url을 기반으로 HTML 문서를 크롤링하고,
     * 해당 전략에 맞는 PostProcessor를 찾아 게시글을 처리
     *
     * @param newsName : 뉴스 출처 이름
     * @param url      : 크롤링할 url
     * @param strategy : 크롤링 전략
     */
    private void crawlAndProcess(String newsName, String url, CrawlingStrategy strategy) {
        log.info("[크롤링 요청] newsName: {}, url: {}", newsName, url);

        Document doc = newsProcessor.fetch(url);

        if (doc == null) {
            log.warn("[크롤링 실패] Document null - newsName: {}, url: {}", newsName, url);
            return;
        }

        Elements elements = strategy.getPostElements(doc);

        if (elements.isEmpty()) {
            log.warn("[소식 내 게시글 없음] newsName: {}, url: {}", newsName, url);
            return;
        }

        postProcessors.stream()
                .filter(processor -> processor.supports(strategy))
                .findFirst()
                .ifPresent(processor -> processor.process(newsName, elements, strategy));
    }
}
