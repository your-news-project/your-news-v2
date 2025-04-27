package kr.co.yournews.domain.news.repository.keyword;

import kr.co.yournews.domain.news.entity.Keyword;

import java.util.List;

public interface CustomKeywordRepository {
    void saveAllInBatch(List<Keyword> keywords);
    void deleteAllByUserId(Long userId);
}
