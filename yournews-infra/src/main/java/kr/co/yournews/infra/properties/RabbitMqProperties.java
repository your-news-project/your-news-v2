package kr.co.yournews.infra.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rabbitmq")
@Getter @Setter
public class RabbitMqProperties {
    private String exchangeName;
    private String routingKey;
}
