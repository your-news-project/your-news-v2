package kr.co.yournews.apis.calendar.service;

import kr.co.yournews.domain.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarCommandService {
    private final CalendarService calendarService;

    /**
     * 오래된 학사 일정을 정리하는 메서드.
     * - 2년 전 학사 일정을 삭제
     */
    @Transactional
    public void deleteCalendarsOlderThanTwoYears() {
        int cutoffYear = LocalDate.now().getYear() - 2;
        calendarService.deleteOldCalendars(cutoffYear);

        log.info("[학사 일정 정리 완료] 삭제된 연도: {}", cutoffYear);
    }
}
