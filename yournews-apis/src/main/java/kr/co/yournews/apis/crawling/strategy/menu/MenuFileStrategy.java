package kr.co.yournews.apis.crawling.strategy.menu;

import kr.co.yournews.apis.crawling.strategy.dto.MenuFileDownloadReq;
import kr.co.yournews.domain.menu.type.MenuType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public interface MenuFileStrategy {

    /**
     * 어떤 식단 타입을 처리하는지 이름 (예: "MAIN_CAFETERIA", "DORMITORY")
     */
    MenuType getMenuName();

    /**
     * 이 전략의 크롤링 스케줄 주기를 반환
     * Spring Cron 표현식 형태
     *
     * @return : 크론 표현식 문자열
     */
    String getScheduledTime();

    /**
     * 크롤링 페이지 URL
     */
    String getUrl();

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
     * 게시글 url을 추출
     *
     * @param postElement : 게시글 HTML 요소
     * @return : 게시글의 url
     */
    String extractPostUrl(Element postElement);

    /**
     * 해당 url이 이미 처리된 적 있는지 확인
     *
     * @param postUrl : 게시글 URL
     * @return : 중복 여부
     */
    boolean isProcessed(String postUrl);

    /**
     * 처리 완료된 게시글 url을 저장
     *
     * @param postUrl : 게시글 URL
     */
    void markProcessed(String postUrl);

    /**
     * 이 전략은 상세 페이지 HTML이 필요한가? (이미지 크롤링 등)
     */
    boolean useDetailPage();

    /**
     * 이 게시글에서 실제 다운로드할 파일들(PDF/이미지) 추출
     *
     * @param postUrl 상세 페이지 URL
     * @param postEl  리스트의 tr 요소
     * @param viewDoc 상세 페이지 HTML (useDetailPage() == true일 때만 세팅, 아니면 null)
     */
    List<MenuFileDownloadReq> extractFiles(
            String postUrl,
            Element postEl,
            Document viewDoc
    );
}
