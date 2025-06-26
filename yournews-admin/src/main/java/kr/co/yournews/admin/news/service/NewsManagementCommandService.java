package kr.co.yournews.admin.news.service;

import kr.co.yournews.admin.news.dto.NewsReq;
import kr.co.yournews.domain.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsManagementCommandService {
    private final NewsService newsService;

    /**
     * 새로운 소식 생성 메서드
     *
     * @param newsReq : 생성하고자 하는 소식 정보 DTO
     */
    @Transactional
    public void createNews(NewsReq newsReq) {
        newsService.save(newsReq.toEntity());
    }

    /**
     * 소식 삭제 메서드
     *
     * @param newsId : 삭제하고자 하는 소식 ID
     */
    @Transactional
    public void deleteNews(Long newsId) {
        newsService.deleteById(newsId);
    }
}
