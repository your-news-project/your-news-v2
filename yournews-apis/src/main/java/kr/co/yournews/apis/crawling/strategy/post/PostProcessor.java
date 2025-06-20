package kr.co.yournews.apis.crawling.strategy.post;

import kr.co.yournews.apis.crawling.strategy.crawling.CrawlingStrategy;
import kr.co.yournews.apis.fcm.dto.FcmMessageDto;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.type.NotificationType;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.infra.fcm.constant.FcmConstant;
import kr.co.yournews.infra.rabbitmq.RabbitMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.jsoup.select.Elements;

import java.util.List;

@RequiredArgsConstructor
public abstract class PostProcessor {
    private final RabbitMessagePublisher rabbitMessagePublisher;

    /**
     * 해당 PostProcessor가 주어진 CrawlingStrategy를 지원하는지 여부를 반환
     *
     * @param strategy : 크롤링 전략
     * @return : 지원 여부
     */
    public abstract boolean supports(CrawlingStrategy strategy);

    /**
     * 주어진 게시글 요소들을 처리 (DB 저장, 알림 전송 등)
     *
     * @param newsName : 소식 이름
     * @param elements : 크롤링된 게시글 요소들
     * @param strategy : 사용된 크롤링 전략
     */
    public abstract void process(String newsName, Elements elements, CrawlingStrategy strategy);

    /**
     * Notification 엔티티 생성
     */
    protected Notification buildNotification(String newsName,
                                             List<String> titles,
                                             List<String> urls,
                                             String publicId,
                                             Long userId) {

        return Notification.builder()
                .newsName(newsName)
                .postTitle(titles)
                .postUrl(urls)
                .publicId(publicId)
                .type(NotificationType.IMMEDIATE)
                .isRead(false)
                .userId(userId)
                .build();
    }

    /**
     * FCM 메시지 전송 (RabbitMQ 이용)
     */
    protected void sendFcmMessages(List<FcmToken> tokens, String newsName, String publicId) {
        String title = FcmConstant.getNewsNotificationTitle(newsName);

        for (FcmToken token : tokens) {
            rabbitMessagePublisher.send(FcmMessageDto.of(token.getToken(), title, publicId));
        }
    }
}
