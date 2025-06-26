package kr.co.yournews.apis.news.service;

import kr.co.yournews.apis.news.dto.NewsInfoDto;
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
     * 전체 뉴스 목록 조회
     *
     * @return : 전체 뉴스의 상세 정보 리스트
     */
    @Transactional(readOnly = true)
    public List<NewsInfoDto> getAllNews() {
        return newsService.readAll()
                .stream().map(NewsInfoDto::from)
                .toList();
    }
}
