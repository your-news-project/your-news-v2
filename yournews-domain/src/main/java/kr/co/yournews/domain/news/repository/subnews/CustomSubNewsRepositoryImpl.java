package kr.co.yournews.domain.news.repository.subnews;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.yournews.domain.news.dto.UserKeywordDto;
import kr.co.yournews.domain.news.entity.QKeyword;
import kr.co.yournews.domain.news.entity.QSubNews;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomSubNewsRepositoryImpl implements CustomSubNewsRepository {
    private final JPAQueryFactory jpaQueryFactory;

    private final QSubNews subNews = QSubNews.subNews;
    private final QKeyword keyword = QKeyword.keyword;

    @Override
    public List<UserKeywordDto> findUserKeywordsByUserIds(List<Long> userIds) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        UserKeywordDto.class,
                        subNews.user.id,
                        keyword.keywordType
                ))
                .from(subNews)
                .join(keyword).on(keyword.subNews.id.eq(subNews.id))
                .where(subNews.user.id.in(userIds))
                .fetch();
    }
}
