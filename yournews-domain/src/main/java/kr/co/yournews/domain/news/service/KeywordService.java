package kr.co.yournews.domain.news.service;

import kr.co.yournews.domain.news.entity.Keyword;
import kr.co.yournews.domain.news.repository.keyword.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordService {
    private final KeywordRepository keywordRepository;

    public void saveAll(List<Keyword> keywords) {
        keywordRepository.saveAllInBatch(keywords);
    }

    public void deleteAllByUserId(Long userId) {
        keywordRepository.deleteAllByUserId(userId);
    }

    public void deleteAllByUserIds(List<Long> userIds) {
        keywordRepository.deleteAllByUserIds(userIds);
    }
}
