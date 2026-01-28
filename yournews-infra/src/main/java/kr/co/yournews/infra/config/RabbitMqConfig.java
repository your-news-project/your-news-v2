package kr.co.yournews.infra.config;

import kr.co.yournews.infra.rabbitmq.RabbitCallbacks;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class RabbitMqConfig {

    /**
     * RabbitTemplate 설정
     */
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            ObjectProvider<RabbitCallbacks> provider
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);

        // 메시지 브로커 발행 ACK/NACK 콜백 등록 (순환참조 방지용 지연 조회)
        template.setConfirmCallback((correlationData, ack, cause) ->
                provider.getObject().handleConfirm(correlationData, ack, cause)
        );

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
