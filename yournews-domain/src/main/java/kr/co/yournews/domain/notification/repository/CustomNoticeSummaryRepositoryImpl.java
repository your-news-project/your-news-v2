package kr.co.yournews.domain.notification.repository;

import kr.co.yournews.domain.notification.entity.NoticeSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CustomNoticeSummaryRepositoryImpl implements CustomNoticeSummaryRepository {
    private final JdbcTemplate jdbcTemplate;

    private final static int BATCH_SIZE = 100;

    /**
     * JdbcTemplate를 이용항 Batch Insert
     *
     * @param noticeSummaries : 저장하고자 하는 noticeSummary 리스트
     */
    @Override
    public void saveAllInBatch(List<NoticeSummary> noticeSummaries) {
        String sql = "INSERT INTO notice_summary ("
                + "url, url_hash, summary, status, created_at, updated_at"
                + ") VALUES (?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.batchUpdate(
                sql,
                noticeSummaries,
                BATCH_SIZE,
                (PreparedStatement ps, NoticeSummary noticeSummary) -> {
                    ps.setString(1, noticeSummary.getUrl());
                    ps.setString(2, noticeSummary.getUrlHash());
                    ps.setString(3, noticeSummary.getSummary());
                    ps.setString(4, noticeSummary.getStatus().name());
                    ps.setTimestamp(5, Timestamp.valueOf(now));
                    ps.setTimestamp(6, Timestamp.valueOf(now));
                }
        );
    }
}
