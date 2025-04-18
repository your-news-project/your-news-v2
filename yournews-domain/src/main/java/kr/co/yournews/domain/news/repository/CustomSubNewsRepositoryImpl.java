package kr.co.yournews.domain.news.repository;

import kr.co.yournews.domain.news.entity.SubNews;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.List;

@RequiredArgsConstructor
public class CustomSubNewsRepositoryImpl implements CustomSubNewsRepository {
    private final JdbcTemplate jdbcTemplate;

    private final static int BATCH_SIZE = 20;

    @Override
    public void saveAllInBatch(List<SubNews> subNewsList) {
        String sql = "INSERT INTO sub_news (user_id, news_id) " +
                "VALUES (?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                subNewsList,
                BATCH_SIZE,
                (PreparedStatement ps, SubNews subNews) -> {
                    ps.setLong(1, subNews.getUser().getId());
                    ps.setLong(2, subNews.getNews().getId());
                });
    }
}
