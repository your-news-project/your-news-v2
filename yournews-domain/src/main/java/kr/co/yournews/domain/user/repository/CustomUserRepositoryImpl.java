package kr.co.yournews.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.yournews.domain.news.entity.QSubNews;
import kr.co.yournews.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {
    private final JPAQueryFactory jpaQueryFactory;

    private final QUser user = QUser.user;
    private final QSubNews subNews = QSubNews.subNews;

    @Override
    public List<Long> findUserIdsByNewsNameAndSubStatusTrue(String newsName) {
        return jpaQueryFactory
                .select(user.id)
                .from(user)
                .join(subNews).on(subNews.user.id.eq(user.id))
                .where(
                        user.subStatus.isTrue(),
                        subNews.newsName.eq(newsName)
                )
                .fetch();
    }

    @Override
    public List<Long> findUserIdsByNewsNameAndDailySubStatus(String newsName) {
        return jpaQueryFactory
                .select(user.id)
                .from(user)
                .join(subNews).on(subNews.user.id.eq(user.id))
                .where(
                        user.dailySubStatus.isTrue(),
                        subNews.newsName.eq(newsName)
                )
                .fetch();
    }
}
