package kr.co.yournews.apis.crawling.service;

import jakarta.annotation.PostConstruct;
import kr.co.yournews.apis.crawling.strategy.crawling.CrawlingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TimeZone;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlingScheduler {
    private final List<CrawlingStrategy> strategies;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final CrawlingExecutor crawlingExecutor;

    /**
     * 각 CrawlingStrategy의 주기에 따라 크롤링 작업을 스케줄링하는 메서드
     * 애플리케이션 시작 시 자동 실행되며, Cron 표현식은 각 전략 내부에 정의
     */
    @PostConstruct
    public void scheduleCrawlingTasks() {
        log.info("[크롤링 스케줄링 등록 시작] 등록된 전략 수: {}", strategies.size());

        strategies.forEach(strategy -> taskScheduler.schedule(
                () -> crawlingExecutor.executeStrategy(strategy),
                new CronTrigger(strategy.getScheduledTime(), TimeZone.getTimeZone("Asia/Seoul"))
        ));

        log.info("[크롤링 스케줄링 등록 완료]");
    }
}
