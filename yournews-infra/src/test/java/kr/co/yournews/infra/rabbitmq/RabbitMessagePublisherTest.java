package kr.co.yournews.infra.rabbitmq;

import kr.co.yournews.infra.properties.RabbitMqProperties;
import kr.co.yournews.infra.rabbitmq.dto.FcmMessageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RabbitMessagePublisherTest {

    @Test
    @DisplayName("메시지 전송 테스트 - PendingPublishStore에 저장하고 convertAndSend를 호출")
    void sendSuccess() {
        // given
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitMqProperties properties = mock(RabbitMqProperties.class);
        PendingPublishStore store = new PendingPublishStore();

        when(properties.getExchangeName()).thenReturn("exchange");
        when(properties.getRoutingKey()).thenReturn("routingKey");

        RabbitMessagePublisher rabbitMessagePublisher = new RabbitMessagePublisher(rabbitTemplate, properties, store);

        FcmMessageDto message = FcmMessageDto.of(
                "token-1", "title", "content", "NOTIFICATION",
                "publicId", true, false
        );

        ArgumentCaptor<CorrelationData> cdCaptor = ArgumentCaptor.forClass(CorrelationData.class);

        // when
        rabbitMessagePublisher.send(message);

        // then
        verify(rabbitTemplate, times(1))
                .convertAndSend(eq("exchange"), eq("routingKey"), eq(message), cdCaptor.capture());

        CorrelationData captured = cdCaptor.getValue();
        assertThat(captured).isNotNull();
        assertThat(captured.getId()).isNotBlank();

        PendingPublish pending = store.get(captured.getId());
        assertThat(pending).isNotNull();
        assertThat(pending.getExchange()).isEqualTo("exchange");
        assertThat(pending.getRoutingKey()).isEqualTo("routingKey");
        assertThat(pending.getMessage()).isEqualTo(message);
    }
}
