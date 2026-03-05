package kr.co.yournews.apis.notification.service;

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
    private final NotificationOutboxService notificationOutboxService;
    private final RabbitMqProperties rabbitMqProperties;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 사용자별 제목 목록을 기반으로 FCM 메시지를 생성해 아웃박스에 배치 저장 메서드
     * - userIdToTitles에 데이터가 없는 사용자 토큰은 제외
     *
     * @param tokens         : 대상 FCM 토큰 목록
     * @param userIdToTitles : 사용자별 공지 제목 목록
     * @param title          : FCM 제목
     * @param target         : FCM 타겟 타입
     * @param info           : FCM 추가 정보
     */
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
            // 토큰 사용자 기준으로 전송할 제목 목록을 조회
            List<String> titles = userIdToTitles.get(token.getUser().getId());
            if (titles == null || titles.isEmpty()) {
                continue;
            }

            // 사용자별 제목 목록을 FCM 본문 문자열로 변환
            String content = FcmMessageFormatter.formatTitles(titles);
            outboxes.add(createPendingOutbox(exchange, routingKey, token.getToken(), title, content, target, info));
        }

        if (!outboxes.isEmpty()) {
            // 아웃박스 배치 저장
            notificationOutboxService.saveAll(outboxes);
        }
    }

    /**
     * 동일 본문(content)을 사용하는 FCM 메시지를 아웃박스에 배치 저장 메서드
     *
     * @param tokens  : 대상 FCM 토큰 목록
     * @param title   : FCM 제목
     * @param content : FCM 본문
     * @param target  : FCM 타겟 타입
     * @param info    : FCM 추가 정보
     */
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
            // 토큰별 payload를 생성해 PENDING outbox로 적재
            outboxes.add(createPendingOutbox(exchange, routingKey, token.getToken(), title, content, target, info));
        }

        // 아웃박스 배치 저장
        notificationOutboxService.saveAll(outboxes);
    }

    /**
     * FCM 메시지를 직렬화해 PENDING 상태의 outbox 엔트리를 생성 메서드
     *
     * @param exchange   : 발행 exchange
     * @param routingKey : 발행 routing key
     * @param token      : FCM 토큰
     * @param title      : FCM 제목
     * @param content    : FCM 본문
     * @param target     : FCM 타겟 타입
     * @param info       : FCM 추가 정보
     * @return : NotificationOutbox 데이터
     */
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

    /**
     * FCM DTO를 JSON payload 문자열로 직렬화
     *
     * @param message : FCM 메시지 DTO
     * @return : JSON 문자열 payload
     */
    private String serialize(FcmMessageDto message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to serialize FcmMessageDto", e);
        }
    }
}
