package kr.co.yournews.apis.crawling.strategy.post;

import kr.co.yournews.apis.crawling.strategy.crawling.CrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.crawling.JobCrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.crawling.YUNewsCrawlingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultPostProcessor implements PostProcessor {

    @Override
    public boolean supports(CrawlingStrategy strategy) {
        return !(strategy instanceof JobCrawlingStrategy) &&
                !(strategy instanceof YUNewsCrawlingStrategy);
    }

    @Override
    public void process(String newsName, Elements elements, CrawlingStrategy strategy) {
        for (Element element : elements) {
            if (!strategy.shouldProcessElement(element)) continue;

            String postTitle = strategy.extractPostTitle(element);
            String postUrl = strategy.extractPostUrl(element);

            log.info("[Default - postTitle] : {}", postTitle);
            log.info("[Default - postURL] : {}", postUrl);

            if (strategy.isExisted(postUrl)) continue;

            // TODO : 알림 로직 구현
            strategy.saveUrl(postUrl);
        }
    }
}
