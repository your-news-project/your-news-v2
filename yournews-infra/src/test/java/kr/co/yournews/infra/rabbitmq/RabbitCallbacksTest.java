package kr.co.yournews.infra.rabbitmq;

import kr.co.yournews.common.sentry.SentryCapture;
import kr.co.yournews.infra.rabbitmq.dto.FcmMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitCallbacksTest {

    private RabbitTemplate rabbitTemplate;
    private ThreadPoolTaskScheduler scheduler;
    private PendingPublishStore store;
    private RabbitCallbacks callbacks;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        scheduler = mock(ThreadPoolTaskScheduler.class);
        store = new PendingPublishStore();
        callbacks = new RabbitCallbacks(rabbitTemplate, scheduler, store);
    }

    private static final String EXCHANGE = "exchange";
    private static final String ROUTING_KEY = "routingKey";

    private CorrelationData givenPending(String messageId) {
        store.put(messageId, EXCHANGE, ROUTING_KEY, defaultMessage());
        return new CorrelationData(messageId);
    }

    private FcmMessageDto defaultMessage() {
        return FcmMessageDto.of(
                "token-1", "title", "content", "NOTIFICATION",
                "publicId", true, false
        );
    }

    @Test
    @DisplayName("메시지 발행 성공(ACK) : pendingStore에서 상태 정보 제거")
    void handleConfirm_whenAck_thenRemovePendingPublish() {
        // given
        CorrelationData cd = givenPending("msg-1");

        // when
        callbacks.handleConfirm(cd, true, null);

        // then
        assertThat(store.get("msg-1")).isNull();
        verify(scheduler, never()).schedule(any(Runnable.class), any(Instant.class));
        verifyNoInteractions(rabbitTemplate); // ✅ 여기서 이게 정답
    }

    @Test
    @DisplayName("메시지 발행 실패(NACK) : MAX_RETRY 이하면 스케줄러에 재시도를 등록")
    void handleConfirm_whenNackAndUnderMaxRetry_thenScheduleRetry() {
        // given
        CorrelationData cd = givenPending("msg-1");
        when(scheduler.schedule(any(Runnable.class), any(Instant.class)))
                .thenReturn(mock(ScheduledFuture.class));

        // when
        callbacks.handleConfirm(cd, false, "cause");

        // then
        PendingPublish pending = store.get("msg-1");
        assertThat(pending).isNotNull();
        assertThat(pending.getAttempt()).isEqualTo(1);

        verify(scheduler, times(1)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    @DisplayName("메시지 발행 실패(NACK) : MAX_RETRY 초과되면 pendingStore 제거 + SentryCapture.warn 호출")
    void handleConfirm_whenNackExceedMaxRetry_thenRemoveAndSendSentry() {
        // given
        CorrelationData cd = givenPending("msg-1");
        when(scheduler.schedule(any(Runnable.class), any(Instant.class)))
                .thenReturn(mock(ScheduledFuture.class));

        try (MockedStatic<SentryCapture> sentry = Mockito.mockStatic(SentryCapture.class)) {

            // when
            callbacks.handleConfirm(cd, false, "cause-1"); // attempt=1
            callbacks.handleConfirm(cd, false, "cause-2"); // attempt=2
            callbacks.handleConfirm(cd, false, "cause-3"); // attempt=3
            callbacks.handleConfirm(cd, false, "cause-4"); // attempt=4 -> remove + sentry

            // then
            assertThat(store.get("msg-1")).isNull();

            sentry.verify(() -> SentryCapture.warn(
                    eq("rabbitmq_publish"),
                    anyMap(),
                    anyMap(),
                    eq("[RABBITMQ][PUBLISH] NACK max retry exceeded")
            ), times(1));

            verify(scheduler, times(3)).schedule(any(Runnable.class), any(Instant.class));
        }
    }
}
