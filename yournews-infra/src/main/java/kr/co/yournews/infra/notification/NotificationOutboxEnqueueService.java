package kr.co.yournews.infra.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.common.util.FcmMessageFormatter;
import kr.co.yournews.domain.notification.entity.NotificationOutbox;
import kr.co.yournews.domain.notification.service.NotificationOutboxService;
import kr.co.yournews.domain.notification.type.OutboxStatus;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.infra.properties.RabbitMqProperties;
import kr.co.yournews.infra.rabbitmq.dto.FcmMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationOutboxEnqueueService {
    private static final int MAX_RETRY_COUNT = 3;

    private final NotificationOutboxService notificationOutboxService;
    private final RabbitMqProperties rabbitMqProperties;
    private final ObjectMapper objectMapper;

    @Transactional
    public void enqueueMessages(
            List<FcmToken> tokens,
            Map<Long, List<String>> userIdToTitles,
            String title,
            String target,
            String info
    ) {
        if (tokens == null || tokens.isEmpty() || userIdToTitles == null || userIdToTitles.isEmpty()) {
            return;
        }

        String exchange = rabbitMqProperties.getExchangeName();
        String routingKey = rabbitMqProperties.getRoutingKey();
        List<NotificationOutbox> outboxes = new ArrayList<>(tokens.size());

        for (FcmToken token : tokens) {
            List<String> titles = userIdToTitles.get(token.getUser().getId());
            if (titles == null || titles.isEmpty()) {
                continue;
            }

            String content = FcmMessageFormatter.formatTitles(titles);
            outboxes.add(createPendingOutbox(exchange, routingKey, token.getToken(), title, content, target, info));
        }

        if (!outboxes.isEmpty()) {
            notificationOutboxService.saveAll(outboxes);
        }
    }

    @Transactional
    public void enqueueMessages(
            List<FcmToken> tokens,
            String title,
            String content,
            String target,
            String info
    ) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        String exchange = rabbitMqProperties.getExchangeName();
        String routingKey = rabbitMqProperties.getRoutingKey();
        List<NotificationOutbox> outboxes = new ArrayList<>(tokens.size());

        for (FcmToken token : tokens) {
            outboxes.add(createPendingOutbox(exchange, routingKey, token.getToken(), title, content, target, info));
        }

        notificationOutboxService.saveAll(outboxes);
    }

    private NotificationOutbox createPendingOutbox(
            String exchange,
            String routingKey,
            String token,
            String title,
            String content,
            String target,
            String info
    ) {
        FcmMessageDto message = FcmMessageDto.of(token, title, content, target, info);
        String payload = serialize(message);

        return NotificationOutbox.builder()
                .exchangeName(exchange)
                .routingKey(routingKey)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .attemptCount(0)
                .maxAttemptCount(MAX_RETRY_COUNT)
                .nextAttemptAt(null)
                .lastError(null)
                .build();
    }

    private String serialize(FcmMessageDto message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to serialize FcmMessageDto", e);
        }
    }
}
