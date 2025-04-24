package kr.co.yournews.infra.rabbitmq;

import kr.co.yournews.infra.properties.RabbitMqProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMessagePublisher {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties rabbitMqProperties;

    /**
     * 지정된 Exchange와 Routing Key로 메시지를 전송한다.
     *
     * @param message : 전송할 메시지 객체 (JSON으로 직렬화됨)
     */
    public void send(Object message) {
        rabbitTemplate.convertAndSend(
                rabbitMqProperties.getExchangeName(), rabbitMqProperties.getRoutingKey(), message
        );
    }
}
