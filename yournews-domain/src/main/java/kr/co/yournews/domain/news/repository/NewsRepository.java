package kr.co.yournews.domain.news.repository;

import kr.co.yournews.domain.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
}
