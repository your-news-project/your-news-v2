package kr.co.yournews.domain.news.repository.news;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.yournews.domain.news.dto.NewsQueryDto;
import kr.co.yournews.domain.news.entity.QNews;
import kr.co.yournews.domain.news.entity.QSubNews;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CustomNewsRepositoryImpl implements CustomNewsRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<NewsQueryDto> findNewsDetailsById(Long newsId) {
        QNews news = QNews.news;
        QSubNews subNews = QSubNews.subNews;

        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.constructor(
                        NewsQueryDto.class,
                        news.id,
                        news.name,
                        news.url,
                        news.college,
                        JPAExpressions
                                .select(subNews.count())
                                .from(subNews)
                                .where(subNews.news.id.eq(news.id))
                ))
                .from(news)
                .where(news.id.eq(newsId))
                .fetchOne());
    }
}
