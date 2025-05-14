package kr.co.yournews.domain.news.repository.subnews;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.yournews.domain.news.dto.SubNewsQueryDto;
import kr.co.yournews.domain.news.dto.UserKeywordDto;
import kr.co.yournews.domain.news.entity.QKeyword;
import kr.co.yournews.domain.news.entity.QSubNews;
import kr.co.yournews.domain.news.type.KeywordType;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public List<SubNewsQueryDto> findSubNewsWithKeywordsByUserId(Long userId) {
        List<Tuple> tuples = jpaQueryFactory
                .select(subNews.newsName, keyword.keywordType)
                .from(subNews)
                .leftJoin(keyword).on(keyword.subNews.eq(subNews))
                .where(subNews.user.id.eq(userId))
                .fetch();

        Map<String, List<KeywordType>> grouped = new LinkedHashMap<>();
        for (Tuple t : tuples) {
            String newsName = t.get(subNews.newsName);
            KeywordType keywordType = t.get(keyword.keywordType);
            grouped.computeIfAbsent(newsName, k -> new ArrayList<>());

            if (keywordType != null) {
                grouped.get(newsName).add(keywordType);
            }
        }

        return grouped.entrySet().stream()
                .map(e -> new SubNewsQueryDto(e.getKey(), e.getValue()))
                .toList();
    }
}
