package kr.co.yournews.apis.fcm.consumer;

import kr.co.yournews.apis.fcm.dto.FcmMessageDto;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.infra.fcm.FcmNotificationSender;
import kr.co.yournews.infra.fcm.constant.FcmConstant;
import kr.co.yournews.infra.fcm.exception.FcmSendFailureException;
import kr.co.yournews.infra.fcm.response.FcmSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FcmNotificationConsumer {
    private final FcmNotificationSender fcmNotificationSender;
    private final FcmTokenService fcmTokenService;

    /**
     * RabbitMQ로부터 수신된 FCM 메시지를 처리하는 메서드
     *
     * 1. FCM 서버에 푸시 알림을 전송
     * 2. 전송 결과에 따라 유효하지 않은 토큰을 삭제
     * 3. 전송 실패 시 RuntimeException을 발생시켜 재시도 처리를 유도함
     *
     * @param message : (FCM 토큰, 알림 제목, 알림 내용)
     */
    @RabbitListener(queues = "${rabbitmq.queue-name}", containerFactory = "rabbitListenerContainerFactory")
    public void handleMessage(FcmMessageDto message) {
        String content = FcmConstant.NEWS_NOTIFICATION_CONTENT;
        Map<String, String> data = buildMessageData(message.data());

        FcmSendResult result = fcmNotificationSender.sendNotification(
                message.token(), message.title(), content, data
        );

        if (result.shouldRemoveToken()) {
            fcmTokenService.deleteByToken(message.token());
            return;
        }

        if (!result.success()) {
            throw new FcmSendFailureException(result.message());
        }
    }

    /**
     * Notification data 생성 메서드
     *
     * @param publicId : 알림 추가 데이터
     * @return data가 저장된 Map 자료구조
     */
    private Map<String, String> buildMessageData(String publicId) {
        Map<String, String> data = new HashMap<>();
        data.put("publicId", publicId);
        return data;
    }
}
