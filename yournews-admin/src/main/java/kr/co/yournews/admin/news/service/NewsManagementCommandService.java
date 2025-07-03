package kr.co.yournews.admin.news.service;

import kr.co.yournews.admin.news.dto.NewsReq;
import kr.co.yournews.domain.news.service.NewsService;
import kr.co.yournews.domain.news.service.SubNewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsManagementCommandService {
    private final NewsService newsService;
    private final SubNewsService subNewsService;

    /**
     * 새로운 소식 생성 메서드
     *
     * @param newsReq : 생성하고자 하는 소식 정보 DTO
     */
    @Transactional
    public void createNews(NewsReq newsReq) {
        log.info("[ADMIN 소식 생성 요청] newsName: {}", newsReq.name());
        newsService.save(newsReq.toEntity());
        log.info("[ADMIN 소식 생성 완료] newsName: {}", newsReq.name());
    }

    /**
     * 소식 삭제 메서드
     * - 구독자 삭제 후 소식 삭제
     *
     * @param newsId : 삭제하고자 하는 소식 ID
     */
    @Transactional
    public void deleteNews(Long newsId) {
        subNewsService.deleteAllByNewsId(newsId);
        newsService.deleteById(newsId);
        log.info("[ADMIN 소식 삭제 완료] newsId: {}", newsId);
    }
}
