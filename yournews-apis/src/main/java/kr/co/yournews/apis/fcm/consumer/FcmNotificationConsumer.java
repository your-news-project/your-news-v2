package kr.co.yournews.apis.fcm.consumer;

import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.infra.fcm.FcmNotificationSender;
import kr.co.yournews.infra.fcm.response.FcmSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
     * @param token   : 알림을 수신할 디바이스의 FCM 토큰
     * @param title   : 알림 제목
     * @param content : 알림 본문 내용
     */
    public void handleMessage(String token, String title, String content) {
        FcmSendResult result = fcmNotificationSender.sendNotification(token, title, content);

        if (result.shouldRemoveToken()) {
            fcmTokenService.deleteByToken(token);
            return;
        }

        if (!result.success()) {
            throw new RuntimeException(result.message());   // TODO : RabbitMQ 도입 후 적절히 수정
        }
    }
}
