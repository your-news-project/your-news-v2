package kr.co.yournews.apis.calendar.dto;

public record CalendarRaw(
        String startDt,
        String endDt,
        String text,
        Long articleNo
) {
}
