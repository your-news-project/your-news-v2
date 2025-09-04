package kr.co.yournews.apis.crawling.strategy.notice;

import kr.co.yournews.apis.crawling.strategy.dto.NewsDetail;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DefaultNoticeStrategy implements NoticeStrategy {

    private static final Set<String> SKIP = Set.of("YuTopia(비교과)", "취업처", "반도체특성화대학", "AI/SW트랙");

    @Override
    public boolean supports(String newsName) { return !SKIP.contains(newsName); }

    @Override
    public NewsDetail extract(Document doc, String newsName, String url) {
        String title = doc.select(".b-title").text();

        String content = doc.select(".b-content-box .fr-view").text();

        return NewsDetail.of(title, content);
    }
}
