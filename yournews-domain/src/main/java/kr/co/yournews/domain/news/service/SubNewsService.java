package kr.co.yournews.domain.news.service;

import kr.co.yournews.domain.news.dto.SubNewsQueryDto;
import kr.co.yournews.domain.news.dto.UserKeywordDto;
import kr.co.yournews.domain.news.entity.SubNews;
import kr.co.yournews.domain.news.repository.subnews.SubNewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubNewsService {
    private final SubNewsRepository subNewsRepository;

    public void save(SubNews subNews) {
        subNewsRepository.save(subNews);
    }

    public void saveAll(List<SubNews> subNewsList) {
        subNewsRepository.saveAll(subNewsList);
    }

    public List<SubNews> readByUserId(Long userId) {
        return subNewsRepository.findByUser_Id(userId);
    }

    public List<UserKeywordDto> readUserKeywordsByUserIds(List<Long> userIds) {
        return subNewsRepository.findUserKeywordsByUserIds(userIds);
    }

    public List<SubNewsQueryDto> readSubNewsWithKeywordsByUserId(Long userId) {
        return subNewsRepository.findSubNewsWithKeywordsByUserId(userId);
    }

    public void deleteAllByUserId(Long userId) {
        subNewsRepository.deleteAllByUserId(userId);
    }

    public void deleteAllByUserIds(List<Long> userIds) {
        subNewsRepository.deleteAllByUserIds(userIds);
    }

    public void deleteAllByNewsId(Long newsId) {
        subNewsRepository.deleteAllByNewsId(newsId);
    }
}
