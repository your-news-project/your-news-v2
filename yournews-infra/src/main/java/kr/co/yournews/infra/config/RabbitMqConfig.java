package kr.co.yournews.infra.config;

import kr.co.yournews.infra.rabbitmq.RabbitPublishConfirmHandler;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    /**
     * RabbitTemplate 설정
     */
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            ObjectProvider<RabbitPublishConfirmHandler> provider
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);

        template.setMandatory(true);

        // 메시지 브로커 발행 ACK/NACK 콜백 등록
        template.setConfirmCallback((correlationData, ack, cause) -> {
            RabbitPublishConfirmHandler handler = provider.getIfAvailable();
            if (handler == null) {
                return;
            }

            String correlationId = (correlationData == null ? null : correlationData.getId());
            handler.handleConfirm(correlationId, ack, cause);
        });

        // 메시지기 queue에 매칭되지 않아 반환될 때
        template.setReturnsCallback(returned -> {
            RabbitPublishConfirmHandler handler = provider.getIfAvailable();
            if (handler == null) {
                return;
            }

            String correlationId = returned.getMessage().getMessageProperties().getMessageId();
            String reason = "replyCode=" + returned.getReplyCode()
                    + ", replyText=" + returned.getReplyText()
                    + ", exchange=" + returned.getExchange()
                    + ", routingKey=" + returned.getRoutingKey();
            handler.handleReturned(correlationId, reason);
        });

        return template;
    }

    /**
     * 메시지 직렬화: Java <-> JSON
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
