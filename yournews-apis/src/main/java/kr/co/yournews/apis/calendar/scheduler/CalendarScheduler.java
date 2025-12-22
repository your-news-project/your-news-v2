package kr.co.yournews.apis.calendar.scheduler;

import kr.co.yournews.apis.calendar.service.CalendarCommandService;
import kr.co.yournews.apis.calendar.service.CalendarSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarScheduler {
    private final CalendarSyncService calendarSyncService;
    private final CalendarCommandService calendarCommandService;

    /**
     * 토요일 오전 03시에 시작하여
     * 학사 일정을 동기화하는 스케줄링 메서드
     */
    @Scheduled(cron = "0 0 3 ? * SAT")
    public void syncCalendarJob() {
        log.info("[학사 일정 스케줄 시작] 동기화");
        calendarSyncService.syncCalendars();
    }

    /**
     * 매년 1월 1일 오전 00시 10분에 시작하여
     * 학사 일정을 정리하는 스케줄링 메서드
     */
    @Scheduled(cron = "0 10 0 1 1 *")
    public void cleanupOldCalendarJob() {
        log.info("[학사 일정 정리 스케줄 시작]");
        calendarCommandService.deleteCalendarsOlderThanTwoYears();
    }
}
