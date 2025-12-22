package kr.co.yournews.apis.calendar.dto;

import kr.co.yournews.domain.calendar.entity.Calendar;
import kr.co.yournews.domain.calendar.type.CalendarType;

import java.time.LocalDate;

public record CalendarRes(
        String title,
        Long articleNo,
        LocalDate startAt,
        LocalDate endAt,
        CalendarType type
) {
    public static CalendarRes from(Calendar calendar) {
        return new CalendarRes(
                calendar.getTitle(),
                calendar.getArticleNo(),
                calendar.getStartAt(),
                calendar.getEndAt(),
                calendar.getType()
        );
    }
}
