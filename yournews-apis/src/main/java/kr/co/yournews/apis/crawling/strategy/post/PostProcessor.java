package kr.co.yournews.apis.crawling.strategy.post;

import kr.co.yournews.apis.crawling.strategy.crawling.CrawlingStrategy;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.type.NotificationType;
import org.jsoup.select.Elements;

import java.util.List;

public abstract class PostProcessor {
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

    protected Notification buildNotification(String newsName, String title, String url) {
        return Notification.builder()
                .newsName(newsName)
                .postTitle(List.of(title))
                .postUrl(List.of(url))
                .type(NotificationType.IMMEDIATE)
                .isRead(false)
//                .user()   추후 조회 작성 후, 삽입
                .build();
    }
}
