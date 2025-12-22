package kr.co.yournews.domain.calendar.service;

import kr.co.yournews.domain.calendar.entity.Calendar;
import kr.co.yournews.domain.calendar.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository calendarRepository;

    public void saveAll(List<Calendar> calendars) {
        calendarRepository.saveAllInBatch(calendars);
    }

    public List<Calendar> readAllByYears(Set<Integer> years) {
        return calendarRepository.findAllByYears(years);
    }

    public List<Calendar> readAllByMonth(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        return calendarRepository.findAllByMonth(startOfMonth, endOfMonth);
    }

    public void deleteAllByIdInBatch(List<Long> ids) {
        calendarRepository.deleteAllByIdInBatch(ids);
    }

    public void deleteOldCalendars(int year) {
        calendarRepository.deleteAllOlderThanYear(year);
    }
}
