package kr.co.yournews.apis.crawling.processing;

import kr.co.yournews.apis.crawling.service.NoticeDetailCrawlingExecutor;
import kr.co.yournews.apis.crawling.strategy.board.BoardStrategy;
import kr.co.yournews.apis.notification.constant.FcmTarget;
import kr.co.yournews.apis.notification.constant.NotificationConstant;
import kr.co.yournews.infra.rabbitmq.dto.FcmMessageDto;
import kr.co.yournews.common.util.HashUtil;
import kr.co.yournews.domain.notification.entity.NoticeSummary;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.service.NoticeSummaryService;
import kr.co.yournews.domain.notification.type.NotificationType;
import kr.co.yournews.domain.notification.type.SummaryStatus;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.infra.rabbitmq.RabbitMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public abstract class PostProcessor {
    private final RabbitMessagePublisher rabbitMessagePublisher;
    private final NoticeSummaryService noticeSummaryService;
    private final NoticeDetailCrawlingExecutor noticeDetailCrawlingExecutor;

    private static final Set<String> SKIP = Set.of("YuTopia(비교과)", "취업처");

    /**
     * 해당 PostProcessor가 주어진 CrawlingStrategy를 지원하는지 여부를 반환
     *
     * @param strategy : 크롤링 전략
     * @return : 지원 여부
     */
    public abstract boolean supports(BoardStrategy strategy);

    /**
     * 주어진 게시글 요소들을 처리 (DB 저장, 알림 전송 등)
     *
     * @param newsName : 소식 이름
     * @param elements : 크롤링된 게시글 요소들
     * @param strategy : 사용된 크롤링 전략
     */
    public abstract void process(String newsName, Elements elements, BoardStrategy strategy);

    /**
     * Notification 엔티티 생성
     */
    protected Notification buildNotification(
            String newsName,
            List<String> titles,
            List<String> urls,
            String publicId,
            Long userId
    ) {
        return Notification.builder()
                .newsName(newsName)
                .postTitle(titles)
                .postUrl(urls)
                .publicId(publicId)
                .type(NotificationType.IMMEDIATE)
                .isRead(false)
                .isBookmarked(false)
                .userId(userId)
                .build();
    }

    /**
     * FCM 메시지 전송 (RabbitMQ 이용)
     */
    protected void sendFcmMessages(List<FcmToken> tokens, String newsName, String publicId) {
        log.info("[알림 메시지 큐 전송 시작] 소식명: {}, 토큰 수: {}, publicId: {}", newsName, tokens.size(), publicId);

        String title = NotificationConstant.getNewsNotificationTitle(newsName);

        for (int idx = 0; idx < tokens.size(); idx++) {
            FcmToken token = tokens.get(idx);
            boolean isFirst = (idx == 0); // 첫번째 토큰 여부 판단
            boolean isLast = (idx == tokens.size() - 1); // 마지막 토큰 여부 판단
            rabbitMessagePublisher.send(
                    FcmMessageDto.of(
                            token.getToken(), title, NotificationConstant.NEWS_CONTENT,
                            FcmTarget.NOTIFICATION, publicId, isFirst, isLast
                    )
            );
        }

        log.info("[알림 메시지 큐 전송 완료] 토큰 수: {}, newsName: {}, publicId: {}", tokens.size(), newsName, publicId);
    }

    /**
     * 소식의 내용을 요약하는 및 저장하는 메서드
     */
    protected void summarizeNewsAndSave(String newsName, List<String> urls) {
        // 내용 요약을 진행하지 않는 공지
        if (SKIP.contains(newsName)) {
            return;
        }

        List<NoticeSummary> summaries = new ArrayList<>();

        for (String url : urls) {
            String urlHash = HashUtil.hash(url);

            NoticeSummary summary = NoticeSummary.builder()
                    .url(url)
                    .urlHash(urlHash)
                    .status(SummaryStatus.PENDING)
                    .build();

            summaries.add(summary);
        }

        // 소식 요약 정보를 담기위한 데이터 저장
        noticeSummaryService.saveAll(summaries);
        // 소식 요약 진행 및 요약 정보 업데이트
        noticeDetailCrawlingExecutor.execute(newsName, urls);
    }
}
