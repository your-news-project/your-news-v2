package kr.co.yournews.domain.calendar.repository;

import kr.co.yournews.domain.calendar.entity.Calendar;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;

@RequiredArgsConstructor
public class CustomCalendarRepositoryImpl implements CustomCalendarRepository {
    private final JdbcTemplate jdbcTemplate;

    private final static int BATCH_SIZE = 100;

    /**
     * JdbcTemplate를 이용항 Batch Insert
     *
     * @param calendars : 저장하고자 하는 calendar 리스트
     */
    @Override
    public void saveAllInBatch(List<Calendar> calendars) {
        String sql = "INSERT INTO calendar ("
                + "title, article_no, start_at, end_at, type"
                + ") VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                sql,
                calendars,
                BATCH_SIZE,
                (PreparedStatement ps, Calendar calendar) -> {
                    ps.setString(1, calendar.getTitle());
                    ps.setLong(2, calendar.getArticleNo());
                    ps.setDate(3, Date.valueOf(calendar.getStartAt()));
                    ps.setDate(4, Date.valueOf(calendar.getEndAt()));
                    ps.setString(5, calendar.getType().name());
                }
        );
    }
}
