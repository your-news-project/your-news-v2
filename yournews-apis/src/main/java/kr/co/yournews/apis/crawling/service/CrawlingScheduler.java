package kr.co.yournews.apis.crawling.service;

import jakarta.annotation.PostConstruct;
import kr.co.yournews.infra.crawling.strategy.CrawlingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
public class CrawlingScheduler {
    private final List<CrawlingStrategy> strategies;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final CrawlingExecutor crawlingExecutor;

    @PostConstruct
    public void scheduleCrawlingTasks() {
        strategies.forEach(strategy -> taskScheduler.schedule(
                () -> crawlingExecutor.executeStrategy(strategy),
                new CronTrigger(strategy.getScheduledTime(), TimeZone.getTimeZone("Asia/Seoul"))
        ));
    }
}
