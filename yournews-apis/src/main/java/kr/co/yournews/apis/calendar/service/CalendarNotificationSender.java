package kr.co.yournews.apis.calendar.service;

import kr.co.yournews.apis.notification.constant.FcmTarget;
import kr.co.yournews.infra.rabbitmq.dto.FcmMessageDto;
import kr.co.yournews.domain.calendar.entity.Calendar;
import kr.co.yournews.domain.calendar.service.CalendarService;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.infra.rabbitmq.RabbitMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarNotificationSender {
    private final UserService userService;
    private final CalendarService calendarService;
    private final FcmTokenService fcmTokenService;
    private final RabbitMessagePublisher rabbitMessagePublisher;

    /**
     * 오늘 기준 일정 알림 전송
     */
    public void sendToday() {
        LocalDate date = LocalDate.now();
        sendCalendarNotification(date, true);
    }

    /**
     * 오늘로부터 3일 뒤 일정 알림 전송
     */
    public void sendThreeDaysBefore() {
        LocalDate date = LocalDate.now().plusDays(3);
        sendCalendarNotification(date, false);
    }

    /**
     * 특정 날짜에 시작하는 캘린더 일정이 있는 경우, 구독자에게 전송하는 메서드.
     */
    private void sendCalendarNotification(LocalDate date, boolean isDday) {
        List<Calendar> calendars = calendarService.readAllByStartAt(date);

        if (calendars.isEmpty()) {
            return;
        }

        List<Long> userIds = userService.readIdsByCalendarSubStatusTrue();
        if (userIds.isEmpty()) {
            return;
        }

        List<FcmToken> tokens = fcmTokenService.readAllByUserIds(userIds);

        String title = buildCalendarTitle(isDday, calendars);
        String content = buildCalendarContent(calendars);

        sendFcmMessages(tokens, title, content, date);
    }

    /**
     * FCM 메시지 전송 (RabbitMQ 이용)
     */
    private void sendFcmMessages(
            List<FcmToken> tokens,
            String title,
            String content,
            LocalDate date
    ) {
        log.info("[일정 메시지 큐 전송 시작] 토큰 수: {}, 날짜: {}", tokens.size(), date);

        for (int idx = 0; idx < tokens.size(); idx++) {
            FcmToken token = tokens.get(idx);
            boolean isFirst = (idx == 0);
            boolean isLast = (idx == tokens.size() - 1);
            rabbitMessagePublisher.send(
                    FcmMessageDto.of(
                            token.getToken(), title, content, FcmTarget.CALENDAR,
                            date.toString(), isFirst, isLast
                    )
            );
        }

        log.info("[일정 메시지 큐 전송 완료] 날짜: {}",  date);

    }

    private String buildCalendarTitle(boolean isDday, List<Calendar> calendars) {
        String prefix = isDday ? "[금일]" : "[3일 뒤]";

        String titles = calendars.stream()
                .map(Calendar::getTitle)
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return prefix + " " + titles + " 일정이 있습니다.";
    }

    private String buildCalendarContent(List<Calendar> calendars) {
        String titles = calendars.stream()
                .map(Calendar::getTitle)
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return titles + " 일정이 있습니다.";
    }

}
