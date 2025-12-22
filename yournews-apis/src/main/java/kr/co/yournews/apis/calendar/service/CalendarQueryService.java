package kr.co.yournews.apis.calendar.service;

import kr.co.yournews.apis.calendar.dto.CalendarRes;
import kr.co.yournews.domain.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarQueryService {
    private final CalendarService calendarService;

    /**
     * 특정 연도와 월에 해당하는 학사 일정을 조회하는 메서드.
     *
     * @param year  : 조회할 연도
     * @param month : 조회할 월 (1 ~ 12)
     * @return : 해당 월에 포함된 학사 일정 목록
     */
    @Transactional(readOnly = true)
    public List<CalendarRes> getCalendarsByMonth(int year, int month) {
        return calendarService.readAllByMonth(year, month)
                .stream().map(CalendarRes::from)
                .toList();
    }
}
