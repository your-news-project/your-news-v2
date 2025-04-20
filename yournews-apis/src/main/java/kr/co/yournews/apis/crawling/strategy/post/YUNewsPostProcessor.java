package kr.co.yournews.apis.crawling.strategy.post;

import kr.co.yournews.apis.crawling.strategy.crawling.CrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.crawling.YUNewsCrawlingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class YUNewsPostProcessor implements PostProcessor {

    @Override
    public boolean supports(CrawlingStrategy strategy) {
        return strategy instanceof YUNewsCrawlingStrategy;
    }

    @Override
    public void process(String newsName, Elements elements, CrawlingStrategy strategy) {
        YUNewsCrawlingStrategy yuNewsStrategy = (YUNewsCrawlingStrategy) strategy;

        for (Element element : elements) {
            if (!yuNewsStrategy.shouldProcessElement(element)) continue;

            String postTitle = yuNewsStrategy.extractPostTitle(element);
            String postUrl = yuNewsStrategy.extractPostUrl(element);

            log.info("[YUNes - postTitle] : {}", postTitle);
            log.info("[YUNes - postURL] : {}", postUrl);

            if (yuNewsStrategy.isExisted(postUrl)) continue;

            // TODO : 알림 로직 구현
            yuNewsStrategy.saveUrl(postUrl);
        }
    }
}
