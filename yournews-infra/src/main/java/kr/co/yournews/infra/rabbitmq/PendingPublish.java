package kr.co.yournews.infra.rabbitmq;

import kr.co.yournews.infra.rabbitmq.dto.FcmMessageDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RabbitMQ 발행 메시지 추적을 위한 객체
 */
@Getter
@RequiredArgsConstructor
public class PendingPublish {

    private final String exchange;
    private final String routingKey;
    private final FcmMessageDto message;

    /**
     * 재시도 횟수 (NACK/즉시실패 발생 시 증가)
     */
    private int attempt = 0;

    public int incrementAttempt() {
        return ++attempt;
    }
}
