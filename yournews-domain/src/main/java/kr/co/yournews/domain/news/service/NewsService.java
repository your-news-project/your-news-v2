package kr.co.yournews.domain.news.service;

import kr.co.yournews.domain.news.dto.NewsQueryDto;
import kr.co.yournews.domain.news.entity.News;
import kr.co.yournews.domain.news.repository.news.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Optional<NewsQueryDto> readById(Long id) {
        return newsRepository.findNewsDetailsById(id);
    }

    public List<News> readAllByIds(List<Long> ids) {
        return newsRepository.findAllById(ids);
    }

    public List<News> readAll() {
        return newsRepository.findAll();
    }

    public Page<News> readAll(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    public void deleteById(Long id) {
        newsRepository.deleteById(id);
    }
}
