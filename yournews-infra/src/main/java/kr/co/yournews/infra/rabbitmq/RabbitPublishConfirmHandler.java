package kr.co.yournews.infra.rabbitmq;

public interface RabbitPublishConfirmHandler {
    void handleConfirm(String correlationId, boolean ack, String cause);
    void handleReturned(String correlationId, String reason);
}
