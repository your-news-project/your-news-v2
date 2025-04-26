package kr.co.yournews.domain.notification.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.domain.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CustomNotificationRepositoryImpl implements CustomNotificationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private final static int BATCH_SIZE = 100;

    /**
     * JdbcTemplate를 이용항 Batch Insert
     *
     * @param notifications : 저장하고자 하는 notification 리스트
     */
    @Override
    public void saveAllInBatch(List<Notification> notifications) {
        String sql = "INSERT INTO notification ("
                + "news_name, post_title, post_url, is_read, type, user_id, public_id, created_at, updated_at"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";


        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.batchUpdate(
                sql,
                notifications,
                BATCH_SIZE,
                (PreparedStatement ps, Notification notification) -> {
                    ps.setString(1, notification.getNewsName());
                    ps.setString(2, convertListToJson(notification.getPostTitle()));
                    ps.setString(3, convertListToJson(notification.getPostUrl()));
                    ps.setBoolean(4, notification.isRead());
                    ps.setString(5, notification.getType().name());
                    ps.setLong(6, notification.getUserId());
                    ps.setString(7, notification.getPublicId());
                    ps.setTimestamp(8, Timestamp.valueOf(now));
                    ps.setTimestamp(9, Timestamp.valueOf(now));
                }
        );
    }

    /**
     * List<String>을 JSON 문자열로 변환
     */
    private String convertListToJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize list to JSON", e);
        }
    }
}
