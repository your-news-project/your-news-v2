package kr.co.yournews.apis.crawling.strategy.notice;

import kr.co.yournews.apis.crawling.strategy.dto.NewsDetail;
import org.jsoup.nodes.Document;

public interface NoticeStrategy {
    /**
     * 처리 가능 여부
     */
    boolean supports(String newsName);

    /**
     * 상세 페이지에서 핵심 데이터 추출
     */
    NewsDetail extract(Document doc, String newsName, String url);

}
