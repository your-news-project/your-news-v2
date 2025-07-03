package kr.co.yournews.admin.news.service;

import kr.co.yournews.admin.news.dto.NewsRes;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.exception.NewsErrorType;
import kr.co.yournews.domain.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsManagementQueryService {
    private final NewsService newsService;

    /**
     * 전체 소식 정보 조회 메서드
     *
     * @return : 전체 소식 정보
     */
    @Transactional(readOnly = true)
    public Page<NewsRes.Summary> getAllNews(Pageable pageable) {
        log.info("[ADMIN 소식 목록 조회 요청]");
        return newsService.readAll(pageable)
                .map(NewsRes.Summary::from);
    }

    /**
     * 특정 소식 ID를 기반으로 뉴스 정보를 조회 메서드
     *
     * @param newsId : 조회할 뉴스 ID
     * @return : 뉴스 요약 정보
     * @throws CustomException NOT_FOUND : 뉴스가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public NewsRes.Details getNewsInfoById(Long newsId) {
        log.info("[ADMIN 소식 조회 요청] newsId: {}", newsId);
        return NewsRes.Details.from(
                newsService.readById(newsId)
                        .orElseThrow(() -> new CustomException(NewsErrorType.NOT_FOUND))
        );
    }
}
