package kr.co.yournews.apis.crawling.service;

import kr.co.yournews.domain.news.service.NewsService;
import kr.co.yournews.infra.crawling.NewsProcessor;
import kr.co.yournews.infra.crawling.dto.ParsedPost;
import kr.co.yournews.infra.crawling.strategy.CrawlingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingExecutor {
    private final NewsService newsService;
    private final NewsProcessor newsProcessor;

    @Async
    public void executeStrategy(CrawlingStrategy strategy) {
        newsService.readAll().stream()
                .filter(news -> strategy.canHandle(news.getName()))
                .forEach(news ->
                        crawlAndProcess(news.getName(), news.getUrl(), strategy)
                );
    }

    private void crawlAndProcess(String newsName, String url, CrawlingStrategy strategy) {
        List<ParsedPost> parsedPosts = newsProcessor.process(url, strategy);

        for (ParsedPost post : parsedPosts) {
            log.info("[Crawling post title] : {}", post.title());
            log.info("[Crawling post url] : {}", post.url());
        }
    }
}
