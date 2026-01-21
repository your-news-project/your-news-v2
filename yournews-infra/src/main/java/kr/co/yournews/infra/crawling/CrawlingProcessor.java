package kr.co.yournews.infra.crawling;

import kr.co.yournews.common.sentry.SentryCapture;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class CrawlingProcessor {

    private static final int MAX_RETRIES = 2;

    /**
     * 주어진 url에서 Jsoup을 이용해 HTML 문서를 가져오는 메서드
     * 최대 MAX_RETRIES까지 재시도하며, 실패 시 null을 반환
     *
     * @param url : HTML 문서를 크롤링할 대상 URL
     * @return : Jsoup Document (HTML 파싱 결과), 실패 시 null
     */
    public Document fetch(String url) {
        int retryCount = 0;
        while (retryCount <= MAX_RETRIES) {
            try {
                return Jsoup.connect(url).get();

            } catch (Exception e) {
                log.error("Error crawling {}: retry {}/{}", url, retryCount++, MAX_RETRIES);

                if (retryCount > MAX_RETRIES) {
                    SentryCapture.warn(
                            "crawling",
                            Map.of(
                                    "stage", "fetch",
                                    "reason", "max_retries"
                            ),
                            Map.of(
                                    "url", url,
                                    "retryCount", retryCount,
                                    "exceptionClass", e.getClass().getName()
                            ),
                            "[CRAWLING] max retries exceeded"
                    );
                    log.warn("[CRAWLING] Max retries reached: {}", url);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return null;
    }
}
