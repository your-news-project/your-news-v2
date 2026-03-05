package kr.co.yournews.infra.rabbitmq;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RabbitMessagePublisherTest {

    @Test
    @DisplayName("메시지 전송 테스트 - send를 호출")
    void sendSuccess() {
        // given
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitMessagePublisher rabbitMessagePublisher = new RabbitMessagePublisher(rabbitTemplate);

        ArgumentCaptor<CorrelationData> cdCaptor = ArgumentCaptor.forClass(CorrelationData.class);
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

        // when
        rabbitMessagePublisher.send("outbox-1", "exchange", "routingKey", "{\"token\":\"token-1\"}");

        // then
        verify(rabbitTemplate, times(1))
                .send(eq("exchange"), eq("routingKey"), messageCaptor.capture(), cdCaptor.capture());

        CorrelationData captured = cdCaptor.getValue();
        assertThat(captured).isNotNull();
        assertThat(captured.getId()).isEqualTo("outbox-1");
        assertThat(messageCaptor.getValue().getMessageProperties().getMessageId()).isEqualTo("outbox-1");
    }
}
