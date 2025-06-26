package kr.co.yournews.domain.news.repository.news;

import kr.co.yournews.domain.news.dto.NewsQueryDto;

import java.util.Optional;

public interface CustomNewsRepository {
    Optional<NewsQueryDto> findNewsDetailsById(Long newsId);
}
