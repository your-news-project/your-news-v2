package kr.co.yournews.domain.calendar.repository;

import kr.co.yournews.domain.calendar.entity.Calendar;

import java.util.List;

public interface CustomCalendarRepository {
    void saveAllInBatch(List<Calendar> calendars);
}
