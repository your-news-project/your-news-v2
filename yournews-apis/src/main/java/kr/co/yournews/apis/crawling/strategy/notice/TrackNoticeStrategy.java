package kr.co.yournews.apis.crawling.strategy.notice;

import kr.co.yournews.apis.crawling.strategy.dto.NewsDetail;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TrackNoticeStrategy implements NoticeStrategy {

    private static final Set<String> NAMES = Set.of("반도체특성화대학", "AI/SW트랙");

    @Override
    public boolean supports(String newsName) { return NAMES.contains(newsName); }

    @Override
    public NewsDetail extract(Document doc, String newsName, String url) {
        String title = doc.select(".dk_view h2").text();

        String content = doc.select(".dk_content .content").text();

        return NewsDetail.of(title, content);
    }
}
