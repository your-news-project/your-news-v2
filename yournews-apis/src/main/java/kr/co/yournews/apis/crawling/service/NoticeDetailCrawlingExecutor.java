package kr.co.yournews.apis.crawling.service;

import kr.co.yournews.apis.crawling.strategy.dto.NewsDetail;
import kr.co.yournews.apis.crawling.strategy.notice.NoticeStrategy;
import kr.co.yournews.apis.noticesummary.service.NoticeSummaryCommandService;
import kr.co.yournews.infra.crawling.CrawlingProcessor;
import kr.co.yournews.infra.openai.NoticeSummaryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeDetailCrawlingExecutor {
    private final CrawlingProcessor crawlingProcessor;
    private final List<NoticeStrategy> newsStrategies;
    private final NoticeSummaryClient noticeSummaryClient;
    private final NoticeSummaryCommandService noticeSummaryCommandService;
    private final @Qualifier("detailExecutor") Executor detailExecutor;

    /**
     * 여러 URL에 대해 상세 크롤링 + 요약 요청 진행 메서드
     *
     * @param newsName  : 뉴스/공지 출처 이름
     * @param urls      : 상세 게시글 URL 목록
     * @param urlHashes : 각 URL의 해시 (DB 매핑 키로 사용)
     */
    public void execute(String newsName, List<String> urls, List<String> urlHashes) {
        IntStream.range(0, urls.size())
                .forEach(i -> {
                    detailExecutor.execute(() -> {
                        process(newsName, urls.get(i), urlHashes.get(i)); // URL별 트랜잭션 커밋
                    });
                });
    }

    /**
     * 단일 URL을 처리하는 메서드
     * - HTML 문서 크롤링
     * - 해당 뉴스 전략으로 게시글 상세 내용 추출
     * - GPT를 통한 요약 생성
     * - DB에 요약본 저장
     */
    private void process(String newsName, String url, String urlHash) {
        log.info("[게시글 상세 크롤링 요청] newsName: {}, url: {}", newsName, url);
        Document doc = crawlingProcessor.fetch(url);

        if (doc == null) {
            log.warn("[게시글 상세 크롤링 실패] Document null - newsName: {}, url: {}", newsName, url);
            return;
        }

        NewsDetail newsDetail = newsStrategies.stream()
                .filter(strategy -> strategy.supports(newsName))
                .findFirst()
                .map(strategy -> strategy.extract(doc, newsName, url))
                .orElse(null);

        // 처리할 내역이 없으면 중단
        if (newsDetail == null) return;

        // 내용 요약본
        String summary = noticeSummaryClient.requestNewsSummary(
                newsDetail.title(), newsDetail.content());

        // 요약본 저장
        noticeSummaryCommandService.saveSummaryInfo(urlHash, summary);

        log.info("[게시글 상세 크롤링 완료] newsName: {}, url: {}", newsName, url);
    }
}
