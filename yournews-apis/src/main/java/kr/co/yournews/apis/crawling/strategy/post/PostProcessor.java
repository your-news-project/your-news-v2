package kr.co.yournews.apis.crawling.strategy.post;

import kr.co.yournews.apis.crawling.strategy.crawling.CrawlingStrategy;
import org.jsoup.select.Elements;

public interface PostProcessor {
    /**
     * 해당 PostProcessor가 주어진 CrawlingStrategy를 지원하는지 여부를 반환
     *
     * @param strategy : 크롤링 전략
     * @return : 지원 여부
     */
    boolean supports(CrawlingStrategy strategy);

    /**
     * 주어진 게시글 요소들을 처리 (DB 저장, 알림 전송 등)
     *
     * @param newsName : 소식 이름
     * @param elements : 크롤링된 게시글 요소들
     * @param strategy : 사용된 크롤링 전략
     */
    void process(String newsName, Elements elements, CrawlingStrategy strategy);
}
