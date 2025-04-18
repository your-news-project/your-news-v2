package kr.co.yournews.domain.news.service;

import kr.co.yournews.domain.news.entity.SubNews;
import kr.co.yournews.domain.news.repository.SubNewsRepository;
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
        subNewsRepository.saveAllInBatch(subNewsList);
    }

    public List<SubNews> readByUserId(Long userId) {
        return subNewsRepository.findByUser_Id(userId);
    }

    public void deleteAllByUserId(Long userId) {
        subNewsRepository.deleteAllByUserId(userId);
    }
}
