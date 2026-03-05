package kr.co.yournews.apis.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import kr.co.yournews.domain.notification.service.NotificationOutboxService;
import kr.co.yournews.domain.notification.type.OutboxStatus;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.infra.properties.RabbitMqProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationOutboxEnqueueServiceTest {

    @Mock
    private NotificationOutboxService notificationOutboxService;

    @Mock
    private RabbitMqProperties rabbitMqProperties;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationOutboxEnqueueService notificationOutboxEnqueueService;

    @Test
    @DisplayName("사용자별 제목 기반 enqueue - 제목 없는 사용자 제외 후 배치 저장")
    void enqueueMessagesByTitlesSuccess() throws Exception {
        // given
        FcmToken validToken = mockTokenWithUser(1L, "token-1");
        FcmToken skippedToken = mockTokenWithUserOnly(2L);

        Map<Long, List<String>> userIdToTitles = Map.of(
                1L, List.of("공지1", "공지2"),
                2L, List.of()
        );

        given(rabbitMqProperties.getExchangeName()).willReturn("exchange.notification");
        given(rabbitMqProperties.getRoutingKey()).willReturn("notification.key");
        given(objectMapper.writeValueAsString(any())).willReturn("{\"ok\":true}");

        // when
        notificationOutboxEnqueueService.enqueueMessages(
                List.of(validToken, skippedToken),
                userIdToTitles,
                "title",
                "CALENDAR",
                "info-value"
        );

        // then
        ArgumentCaptor<List<NotificationOutbox>> outboxesCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationOutboxService, times(1)).saveAll(outboxesCaptor.capture());

        List<NotificationOutbox> saved = outboxesCaptor.getValue();
        assertEquals(1, saved.size());

        NotificationOutbox outbox = saved.get(0);
        assertEquals("exchange.notification", outbox.getExchangeName());
        assertEquals("notification.key", outbox.getRoutingKey());
        assertEquals("{\"ok\":true}", outbox.getPayload());
        assertEquals(OutboxStatus.PENDING, outbox.getStatus());
        assertEquals(0, outbox.getAttemptCount());
        assertEquals(3, outbox.getMaxAttemptCount());
        assertNull(outbox.getNextAttemptAt());
        assertNull(outbox.getLastError());
    }

    @Test
    @DisplayName("동일 본문 enqueue - 토큰 개수만큼 저장")
    void enqueueMessagesWithSameContentSuccess() throws Exception {
        // given
        FcmToken token1 = mockToken("token-1");
        FcmToken token2 = mockToken("token-2");

        given(rabbitMqProperties.getExchangeName()).willReturn("exchange.notification");
        given(rabbitMqProperties.getRoutingKey()).willReturn("notification.key");
        given(objectMapper.writeValueAsString(any())).willReturn("{\"ok\":true}");

        // when
        notificationOutboxEnqueueService.enqueueMessages(
                List.of(token1, token2),
                "title",
                "content",
                "NOTICE",
                "info-value"
        );

        // then
        ArgumentCaptor<List<NotificationOutbox>> outboxesCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationOutboxService, times(1)).saveAll(outboxesCaptor.capture());
        assertEquals(2, outboxesCaptor.getValue().size());
    }

    @Test
    @DisplayName("동일 본문 enqueue - 직렬화 실패 시 IllegalArgumentException")
    void enqueueMessagesWithSameContentSerializationFail() throws Exception {
        // given
        FcmToken token = mockToken("token-1");

        given(rabbitMqProperties.getExchangeName()).willReturn("exchange.notification");
        given(rabbitMqProperties.getRoutingKey()).willReturn("notification.key");
        given(objectMapper.writeValueAsString(any()))
                .willThrow(new JsonProcessingException("serialize fail") {});

        // when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> notificationOutboxEnqueueService.enqueueMessages(
                        List.of(token),
                        "title",
                        "content",
                        "NOTICE",
                        "info-value"
                )
        );

        // then
        assertNotNull(exception.getMessage());
        verify(notificationOutboxService, never()).saveAll(anyList());
    }

    private FcmToken mockTokenWithUser(Long userId, String tokenValue) {
        FcmToken token = org.mockito.Mockito.mock(FcmToken.class);
        User user = org.mockito.Mockito.mock(User.class);

        given(token.getUser()).willReturn(user);
        given(user.getId()).willReturn(userId);
        given(token.getToken()).willReturn(tokenValue);

        return token;
    }

    private FcmToken mockTokenWithUserOnly(Long userId) {
        FcmToken token = org.mockito.Mockito.mock(FcmToken.class);
        User user = org.mockito.Mockito.mock(User.class);

        given(token.getUser()).willReturn(user);
        given(user.getId()).willReturn(userId);

        return token;
    }

    private FcmToken mockToken(String tokenValue) {
        FcmToken token = org.mockito.Mockito.mock(FcmToken.class);
        given(token.getToken()).willReturn(tokenValue);
        return token;
    }
}
