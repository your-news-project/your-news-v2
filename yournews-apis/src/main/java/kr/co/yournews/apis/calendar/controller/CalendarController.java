package kr.co.yournews.apis.calendar.controller;

import kr.co.yournews.apis.calendar.service.CalendarQueryService;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calendars")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarQueryService calendarQueryService;

    @GetMapping
    public ResponseEntity<?> getCalendarsByMonth(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        calendarQueryService.getCalendarsByMonth(year, month)
                )
        );
    }
}
