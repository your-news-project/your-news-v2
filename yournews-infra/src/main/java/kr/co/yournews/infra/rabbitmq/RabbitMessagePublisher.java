package kr.co.yournews.infra.rabbitmq;

import kr.co.yournews.infra.properties.RabbitMqProperties;
import kr.co.yournews.infra.rabbitmq.dto.FcmMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RabbitMessagePublisher {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties rabbitMqProperties;
    private final PendingPublishStore pendingPublishStore;

    /**
     * 지정된 Exchange와 Routing Key로 메시지를 전송하는 메서드
     *
     * @param message : 전송할 메시지 객체 (JSON으로 직렬화됨)
     */
    public void send(FcmMessageDto message) {
        String exchange = rabbitMqProperties.getExchangeName();
        String routingKey = rabbitMqProperties.getRoutingKey();
        String messageId = UUID.randomUUID().toString();

        CorrelationData correlationData = new CorrelationData(messageId);

        // 재시도를 위해 원본 메시지 저장
        pendingPublishStore.put(messageId, exchange, routingKey, message);

        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                message,
                correlationData
        );
    }
}
