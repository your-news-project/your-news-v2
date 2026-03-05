package kr.co.yournews.apis.notification.service;

import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import kr.co.yournews.domain.notification.service.NotificationOutboxService;
import kr.co.yournews.domain.notification.type.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationOutboxResultHandlerTest {

    @Mock
    private NotificationOutboxService notificationOutboxService;

    @InjectMocks
    private NotificationOutboxResultHandler notificationOutboxResultHandler;

    @Test
    @DisplayName("confirm ack=true 이고 IN_PROGRESS면 SENT로 전이")
    void handleConfirmAckSuccess() {
        // given
        NotificationOutbox outbox = createOutbox(OutboxStatus.IN_PROGRESS, 1, 3);
        given(notificationOutboxService.readById(1L)).willReturn(Optional.of(outbox));

        // when
        notificationOutboxResultHandler.handleConfirm("1", true, null);

        // then
        verify(notificationOutboxService, times(1)).readById(1L);
        assertEquals(OutboxStatus.SENT, outbox.getStatus());
        assertNull(outbox.getNextAttemptAt());
        assertNull(outbox.getLastError());
    }

    @Test
    @DisplayName("confirm ack=false 이고 IN_PROGRESS면 재시도(PENDING)로 전이")
    void handleConfirmNackRetry() {
        // given
        NotificationOutbox outbox = createOutbox(OutboxStatus.IN_PROGRESS, 1, 3);
        given(notificationOutboxService.readById(2L)).willReturn(Optional.of(outbox));

        // when
        notificationOutboxResultHandler.handleConfirm("2", false, "nack-cause");

        // then
        assertEquals(OutboxStatus.PENDING, outbox.getStatus());
        assertNotNull(outbox.getNextAttemptAt());
        assertEquals("nack-cause", outbox.getLastError());
    }

    @Test
    @DisplayName("returned 이고 IN_PROGRESS면 재시도(PENDING)로 전이")
    void handleReturnedRetry() {
        // given
        NotificationOutbox outbox = createOutbox(OutboxStatus.IN_PROGRESS, 1, 3);
        given(notificationOutboxService.readById(3L)).willReturn(Optional.of(outbox));

        // when
        notificationOutboxResultHandler.handleReturned("3", "returned-reason");

        // then
        assertEquals(OutboxStatus.PENDING, outbox.getStatus());
        assertNotNull(outbox.getNextAttemptAt());
        assertEquals("returned-reason", outbox.getLastError());
    }

    @Test
    @DisplayName("confirm timeout 처리 시 재시도(PENDING)로 전이")
    void handleConfirmTimeoutRetry() {
        // given
        NotificationOutbox outbox = createOutbox(OutboxStatus.IN_PROGRESS, 1, 3);

        // when
        notificationOutboxResultHandler.handleConfirmTimeout(outbox);

        // then
        assertEquals(OutboxStatus.PENDING, outbox.getStatus());
        assertNotNull(outbox.getNextAttemptAt());
        assertEquals("publish confirm timeout", outbox.getLastError());
    }

    @Test
    @DisplayName("send 단계 예외 처리 시 재시도(PENDING)로 전이")
    void handlePublishExceptionRetry() {
        // given
        NotificationOutbox outbox = createOutbox(OutboxStatus.IN_PROGRESS, 1, 3);

        // when
        notificationOutboxResultHandler.handlePublishException(outbox, new RuntimeException("send-fail"));

        // then
        assertEquals(OutboxStatus.PENDING, outbox.getStatus());
        assertNotNull(outbox.getNextAttemptAt());
        assertEquals("send-fail", outbox.getLastError());
    }

    @Test
    @DisplayName("confirm timeout 처리 시 최대 재시도 도달이면 FAILED로 전이")
    void handleConfirmTimeoutFailWhenRetryExhausted() {
        // given
        NotificationOutbox outbox = createOutbox(OutboxStatus.IN_PROGRESS, 3, 3);

        // when
        notificationOutboxResultHandler.handleConfirmTimeout(outbox);

        // then
        assertEquals(OutboxStatus.FAILED, outbox.getStatus());
        assertNull(outbox.getNextAttemptAt());
        assertEquals("publish confirm timeout", outbox.getLastError());
    }

    @Test
    @DisplayName("correlationId가 숫자가 아니면 조회하지 않고 종료")
    void handleConfirmSkipWhenInvalidCorrelationId() {
        // when
        notificationOutboxResultHandler.handleConfirm("abc", true, null);

        // then
        verify(notificationOutboxService, never()).readById(anyLong());
    }

    @Test
    @DisplayName("outbox가 존재하지 않으면 상태 전이 없이 종료")
    void handleConfirmSkipWhenOutboxNotFound() {
        // given
        given(notificationOutboxService.readById(100L)).willReturn(Optional.empty());

        // when
        notificationOutboxResultHandler.handleConfirm("100", false, "nack");

        // then
        verify(notificationOutboxService, times(1)).readById(100L);
    }

    private NotificationOutbox createOutbox(OutboxStatus status, int attemptCount, int maxAttemptCount) {
        NotificationOutbox outbox = NotificationOutbox.builder()
                .exchangeName("exchange.notification")
                .routingKey("notification.key")
                .payload("{\"ok\":true}")
                .status(status)
                .attemptCount(attemptCount)
                .maxAttemptCount(maxAttemptCount)
                .nextAttemptAt(LocalDateTime.now())
                .lastError(null)
                .build();
        ReflectionTestUtils.setField(outbox, "id", 1L);
        return outbox;
    }
}
