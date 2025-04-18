package kr.co.yournews.apis.news.service;

import kr.co.yournews.apis.news.dto.NewsInfoDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.exception.NewsErrorType;
import kr.co.yournews.domain.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsQueryService {
    private final NewsService newsService;

    /**
     * 특정 뉴스 ID를 기반으로 뉴스 정보를 조회
     *
     * @param newsId : 조회할 뉴스의 pk값
     * @return : 뉴스 요약 정보 DTO
     * @throws CustomException NOT_FOUND : 뉴스가 존재하지 않는 경우
     */
    @Transactional(readOnly = true)
    public NewsInfoDto.Summary getNewsInfo(Long newsId) {
        return NewsInfoDto.Summary.from(
                newsService.readById(newsId)
                        .orElseThrow(() -> new CustomException(NewsErrorType.NOT_FOUND))
        );
    }

    /**
     * 전체 뉴스 목록 조회
     *
     * @return : 전체 뉴스의 상세 정보 리스트
     */
    @Transactional(readOnly = true)
    public List<NewsInfoDto.Details> getAllNews() {
        return newsService.readAll()
                .stream().map(NewsInfoDto.Details::from)
                .toList();
    }
}
