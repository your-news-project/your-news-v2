package kr.co.yournews.apis.crawling.strategy.crawling;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public interface CrawlingStrategy {
    /**
     * 이 전략의 크롤링 스케줄 주기를 반환
     * Spring Cron 표현식 형태
     *
     * @return : 크론 표현식 문자열
     */
    String getScheduledTime();

    /**
     * 해당 소식 이름으로 구현된 전략이 처리 가능한 대상인지 판단
     *
     * @param newsName : 소식 이름
     * @return : 처리 가능 여부
     */
    boolean canHandle(String newsName);

    /**
     * HTML 문서에서 게시글 요소들을 추출
     *
     * @param doc : Jsoup으로 파싱한 HTML 문서
     * @return : 게시글 요소 목록
     */
    Elements getPostElements(Document doc);

    /**
     * 주어진 게시글 요소가 처리 대상인지 판단
     *
     * @param postElement : 게시글 HTML 요소
     * @return : 처리 여부
     */
    boolean shouldProcessElement(Element postElement);

    /**
     * 게시글 제목을 추출
     *
     * @param postElement : 게시글 HTML 요소
     * @return : 게시글 제목
     */
    String extractPostTitle(Element postElement);

    /**
     * 게시글 url을 추출
     *
     * @param postElement : 게시글 HTML 요소
     * @return : 게시글의 url
     */
    String extractPostUrl(Element postElement);

    /**
     * 해당 뉴스 이름을 구독 중인 사용자들의 이메일 목록을 조회
     *
     * @param newsName : 소식 이름
     * @return : 사용자 pk값
     */
    List<Long> getSubscribedUsers(String newsName);

    /**
     * 처리 완료된 게시글 url을 저장
     *
     * @param postUrl : 게시글 URL
     */
    void saveUrl(String postUrl);

    /**
     * 해당 url이 이미 처리된 적 있는지 확인
     *
     * @param postUrl : 게시글 URL
     * @return : 중복 여부
     */
    boolean isExisted(String postUrl);
}
