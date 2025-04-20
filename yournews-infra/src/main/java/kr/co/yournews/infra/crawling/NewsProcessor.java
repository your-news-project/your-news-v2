package kr.co.yournews.infra.crawling;

import kr.co.yournews.infra.crawling.dto.ParsedPost;
import kr.co.yournews.infra.crawling.strategy.CrawlingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class NewsProcessor {

    private static final int MAX_RETRIES = 2;

    /**
     * 주어진 url을 Jsoup으로 크롤링하고, 전략에 따라 게시글 정보를 파싱하여 반환
     * 실패 시 최대 MAX_RETRIES만큼 재시도하며, 성공하면 즉시 파싱된 결과를 반환
     *
     * @param url      :    크롤링 대상 URL
     * @param strategy : HTML 파싱 전략
     * @return : 파싱된 게시글 목록 (ParsedPost 리스트), 실패 시 빈 리스트 반환
     */
    public List<ParsedPost> process(String url, CrawlingStrategy strategy) {
        int retryCount = 0;
        while (retryCount <= MAX_RETRIES) {
            try {
                Document doc = Jsoup.connect(url).get();

                return strategy.getPostElements(doc).stream()
                        .filter(strategy::shouldProcessElement)
                        .map(element -> new ParsedPost(
                                strategy.extractPostTitle(element),
                                strategy.extractPostURL(element)
                        ))
                        .toList();

            } catch (Exception e) {
                log.error("Error crawling {}: retry {}/{}", url, retryCount++, MAX_RETRIES);

                if (retryCount > MAX_RETRIES) {
                    log.error("Max retries reached: {}", url);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return List.of();
    }

}
