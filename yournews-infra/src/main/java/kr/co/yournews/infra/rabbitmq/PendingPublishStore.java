package kr.co.yournews.infra.rabbitmq;

import kr.co.yournews.infra.rabbitmq.dto.FcmMessageDto;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * RabbitMQ 발행 메시지 추적을 위한 상태 객체 저장소
 */
@Component
public class PendingPublishStore {
    private final ConcurrentHashMap<String, PendingPublish> pendingStore = new ConcurrentHashMap<>();

    public void put(String messageId, String exchange, String routingKey, FcmMessageDto message) {
        pendingStore.put(messageId, new PendingPublish(exchange, routingKey, message));
    }

    public PendingPublish get(String messageId) {
        return pendingStore.get(messageId);
    }

    public void remove(String messageId) {
        pendingStore.remove(messageId);
    }
}
