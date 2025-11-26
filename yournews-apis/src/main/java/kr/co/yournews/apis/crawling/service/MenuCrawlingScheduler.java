package kr.co.yournews.apis.crawling.service;

import jakarta.annotation.PostConstruct;
import kr.co.yournews.apis.crawling.strategy.menu.MenuFileStrategy;
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
public class MenuCrawlingScheduler {
    private final List<MenuFileStrategy> strategies;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final MenuCrawlingExecutor menuCrawlingExecutor;

    /**
     * 각 MenuFileStrategy의 주기에 따라 식단 크롤링 작업을 스케줄링하는 메서드
     * 애플리케이션 시작 시 자동 실행되며, Cron 표현식은 각 전략 내부에 정의
     */
    @PostConstruct
    public void scheduleMenuCrawlingTasks() {
        log.info("[학식 크롤링 스케줄링 등록 시작] 등록된 메뉴 전략 수: {}", strategies.size());

        strategies.forEach(strategy -> taskScheduler.schedule(
                () -> menuCrawlingExecutor.executeStrategy(strategy),
                new CronTrigger(strategy.getScheduledTime(), TimeZone.getTimeZone("Asia/Seoul"))
        ));

        log.info("[학식 크롤링 스케줄링 등록 완료]");
    }
}
