package kr.co.yournews.domain.notification.repository.messageprocess;

import kr.co.yournews.domain.notification.entity.MessageProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageProcessRepository extends JpaRepository<MessageProcess, Long> {
    @Modifying
    @Query("delete from message_process m where m.createdAt < :dateTime")
    int deleteByCreatedAtBefore(@Param("dateTime") LocalDateTime dateTime);

    /**
     * created_at 기준 시간 버킷으로 메시지 처리 결과를 집계한다.
     * DLQ 재처리로 완료 시각이 다음 날로 넘어가도, 최초 생성된 시간대 버킷에 귀속된다.
     * 건수는 전체 최종 결과를 기준으로 집계하고, 시작/종료 시각은 DLQ 미사용 건만 대상으로 계산한다.
     */
    @Query(value = """
            SELECT CAST(DATE_FORMAT(created_at, '%Y-%m-%d %H:00:00') AS DATETIME) AS summaryStartedAt,
                   COUNT(*) AS totalCount,
                   SUM(CASE WHEN status = 'SUCCEEDED' THEN 1 ELSE 0 END) AS successCount,
                   SUM(CASE WHEN status IN ('FAILED_PERMANENT', 'FAILED_FINAL') THEN 1 ELSE 0 END) AS failureCount,
                   SUM(CASE WHEN attempt_count > 1 OR dlq_attempt_count > 0 THEN 1 ELSE 0 END) AS retryCount,
                   SUM(CASE WHEN status = 'FAILED_FINAL' THEN 1 ELSE 0 END) AS dlqFailedCount,
                   MIN(CASE WHEN dlq_attempt_count = 0 THEN created_at END) AS firstCreatedAt,
                   MAX(CASE WHEN dlq_attempt_count = 0 THEN completed_at END) AS lastCompletedAt,
                   COALESCE(
                       TIMESTAMPDIFF(
                           SECOND,
                           MIN(CASE WHEN dlq_attempt_count = 0 THEN created_at END),
                           MAX(CASE WHEN dlq_attempt_count = 0 THEN completed_at END)
                       ),
                       0
                   ) AS processingDurationSeconds
            FROM message_process
            WHERE created_at >= :startDateTime
              AND created_at < :endDateTime
              AND status IN ('SUCCEEDED', 'FAILED_PERMANENT', 'FAILED_FINAL')
              AND completed_at IS NOT NULL
            GROUP BY CAST(DATE_FORMAT(created_at, '%Y-%m-%d %H:00:00') AS DATETIME)
            ORDER BY summaryStartedAt
            """, nativeQuery = true)
    List<MessageProcessHourlyAggregation> findHourlyAggregations(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
