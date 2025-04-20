package kr.co.yournews.apis.crawling.strategy.post;

import kr.co.yournews.apis.crawling.strategy.crawling.CrawlingStrategy;
import kr.co.yournews.apis.crawling.strategy.crawling.JobCrawlingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobPostProcessor implements PostProcessor {

    @Override
    public boolean supports(CrawlingStrategy strategy) {
        return strategy instanceof JobCrawlingStrategy;
    }

    @Override
    public void process(String newsName, Elements elements, CrawlingStrategy strategy) {
        JobCrawlingStrategy jobStrategy = (JobCrawlingStrategy) strategy;

        for (Element element : elements) {
            if (!jobStrategy.shouldProcessElement(element)) continue;

            String postTitle = jobStrategy.extractPostTitle(element);
            String postUrl = jobStrategy.extractPostUrl(element);

            log.info("[Job - postTitle] : {}", postTitle);
            log.info("[Job - postURL] : {}", postUrl);

            if (jobStrategy.isExisted(postUrl)) continue;

            // TODO : 알림 로직 구현
            jobStrategy.saveUrl(postUrl);

        }
    }
}
