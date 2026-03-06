package kr.co.yournews.apis.notification.service;

import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import kr.co.yournews.domain.notification.type.OutboxStatus;
import kr.co.yournews.infra.rabbitmq.RabbitMessagePublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class NotificationOutboxPublishServiceTest {

    @Mock
    private NotificationOutboxProcessingService outboxProcessingService;

    @Mock
    private RabbitMessagePublisher rabbitMessagePublisher;

    @InjectMocks
    private NotificationOutboxPublishService notificationOutboxPublishService;

    @Test
    @DisplayName("정체된 IN_PROGRESS 복구 호출 후 PENDING 메시지 발행 성공")
    void publishPendingMessagesSuccess() {
        // given
        NotificationOutbox pending = createOutbox("exchange.notification", "notification.key", "{\"ok\":true}");
        ReflectionTestUtils.setField(pending, "id", 10L);

        given(outboxProcessingService.claimPendingMessages(any()))
                .willReturn(List.of(pending));

        // when
        notificationOutboxPublishService.publishPendingMessages();

        // then
        verify(outboxProcessingService, times(1)).recoverExpiredInProgressMessages(any());
        verify(outboxProcessingService, times(1)).claimPendingMessages(any());
        verify(rabbitMessagePublisher, times(1)).send(
                eq("10"),
                eq("exchange.notification"),
                eq("notification.key"),
                eq("{\"ok\":true}")
        );
        verify(outboxProcessingService, never()).handlePublishException(anyLong(), any());
    }

    @Test
    @DisplayName("PENDING 대상이 없으면 발행하지 않음")
    void publishPendingMessagesSkipWhenNoTarget() {
        // given
        given(outboxProcessingService.claimPendingMessages(any()))
                .willReturn(List.of());

        // when
        notificationOutboxPublishService.publishPendingMessages();

        // then
        verify(outboxProcessingService, times(1)).recoverExpiredInProgressMessages(any());
        verify(outboxProcessingService, times(1)).claimPendingMessages(any());
        verify(rabbitMessagePublisher, never()).send(any(), any(), any(), any());
        verify(outboxProcessingService, never()).handlePublishException(anyLong(), any());
    }

    @Test
    @DisplayName("발행 중 예외 발생 시 ProcessingService 예외 처리 위임")
    void publishPendingMessagesHandlePublishException() {
        // given
        NotificationOutbox pending = createOutbox("exchange.notification", "notification.key", "{\"ok\":true}");
        ReflectionTestUtils.setField(pending, "id", 20L);

        given(outboxProcessingService.claimPendingMessages(any()))
                .willReturn(List.of(pending));

        doThrow(new RuntimeException("publish fail"))
                .when(rabbitMessagePublisher)
                .send(eq("20"), eq("exchange.notification"), eq("notification.key"), eq("{\"ok\":true}"));

        // when
        notificationOutboxPublishService.publishPendingMessages();

        // then
        verify(outboxProcessingService, times(1)).recoverExpiredInProgressMessages(any());
        verify(outboxProcessingService, times(1)).claimPendingMessages(any());
        verify(outboxProcessingService, times(1)).handlePublishException(eq(20L), any(RuntimeException.class));
    }

    private NotificationOutbox createOutbox(String exchangeName, String routingKey, String payload) {
        return NotificationOutbox.builder()
                .exchangeName(exchangeName)
                .routingKey(routingKey)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .attemptCount(0)
                .maxAttemptCount(3)
                .nextAttemptAt(null)
                .lastError(null)
                .build();
    }
}
