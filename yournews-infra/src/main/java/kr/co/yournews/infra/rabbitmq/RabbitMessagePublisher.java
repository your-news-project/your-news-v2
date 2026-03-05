package kr.co.yournews.infra.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RabbitMessagePublisher {
    private final RabbitTemplate rabbitTemplate;

    public void send(String messageId, String exchange, String routingKey, String payload) {
        CorrelationData correlationData = new CorrelationData(messageId);
        Message message = MessageBuilder
                .withBody(payload.getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setMessageId(messageId)
                .build();

        rabbitTemplate.send(exchange, routingKey, message, correlationData);
    }
}
