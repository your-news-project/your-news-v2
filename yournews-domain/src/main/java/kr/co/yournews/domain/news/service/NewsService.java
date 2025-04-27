package kr.co.yournews.domain.news.service;

import kr.co.yournews.domain.news.entity.News;
import kr.co.yournews.domain.news.repository.news.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    public void save(News news) {
        newsRepository.save(news);
    }

    public Optional<News> readById(Long id) {
        return newsRepository.findById(id);
    }

    public List<News> readAllByIds(List<Long> ids) {
        return newsRepository.findAllById(ids);
    }

    public List<News> readAll() {
        return newsRepository.findAll();
    }

    public void deleteById(Long id) {
        newsRepository.deleteById(id);
    }
}
