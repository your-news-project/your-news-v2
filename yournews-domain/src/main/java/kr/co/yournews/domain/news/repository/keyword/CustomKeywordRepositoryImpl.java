package kr.co.yournews.domain.news.repository.keyword;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.yournews.domain.news.entity.Keyword;
import kr.co.yournews.domain.news.entity.QKeyword;
import kr.co.yournews.domain.news.entity.QSubNews;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;

@RequiredArgsConstructor
public class CustomKeywordRepositoryImpl implements CustomKeywordRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final JdbcTemplate jdbcTemplate;

    private final static int BATCH_SIZE = 20;

    /**
     * JdbcTemplate를 이용항 Batch Insert
     *
     * @param keywords : 저장하고자 하는 Keyword 리스트
     */
    @Override
    public void saveAllInBatch(List<Keyword> keywords) {
        String sql = "INSERT INTO keyword (keyword_type, sub_news_id) " +
                "VALUES (?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                keywords,
                BATCH_SIZE,
                (PreparedStatement ps, Keyword keyword) -> {
                    ps.setString(1, keyword.getKeywordType().name());
                    ps.setLong(2, keyword.getSubNews().getId());
                });
    }

    /**
     * QueryDSL을 이용한 키워드 삭제
     *
     * @param userId : 키워드를 삭제하고자하는 사용자 pk
     */
    @Override
    public void deleteAllByUserId(Long userId) {
        QKeyword keyword = QKeyword.keyword;
        QSubNews subNews = QSubNews.subNews;

        jpaQueryFactory.delete(keyword)
                .where(keyword.subNews.id.in(
                        JPAExpressions
                                .select(subNews.id)
                                .from(subNews)
                                .where(subNews.user.id.eq(userId))
                ))
                .execute();
    }
}
