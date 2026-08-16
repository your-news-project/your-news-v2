package kr.co.yournews.domain.studentservice.repository;

import kr.co.yournews.domain.studentservice.entity.StudentServiceDailyStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface StudentServiceDailyStatRepository
        extends JpaRepository<StudentServiceDailyStat, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO student_service_daily_stat
                (student_service_id, stat_date, view_count, click_count, like_count)
            VALUES (:studentServiceId, :statDate, 1, 0, 0)
            ON DUPLICATE KEY UPDATE view_count = view_count + 1
            """, nativeQuery = true)
    void increaseViewCount(
            @Param("studentServiceId") Long studentServiceId,
            @Param("statDate") LocalDate statDate
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO student_service_daily_stat
                (student_service_id, stat_date, view_count, click_count, like_count)
            VALUES (:studentServiceId, :statDate, 0, 1, 0)
            ON DUPLICATE KEY UPDATE click_count = click_count + 1
            """, nativeQuery = true)
    void increaseClickCount(
            @Param("studentServiceId") Long studentServiceId,
            @Param("statDate") LocalDate statDate
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO student_service_daily_stat
                (student_service_id, stat_date, view_count, click_count, like_count)
            VALUES (:studentServiceId, :statDate, 0, 0, 1)
            ON DUPLICATE KEY UPDATE like_count = like_count + 1
            """, nativeQuery = true)
    void increaseLikeCount(
            @Param("studentServiceId") Long studentServiceId,
            @Param("statDate") LocalDate statDate
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE student_service_daily_stat stat
            SET stat.likeCount = CASE WHEN stat.likeCount > 0 THEN stat.likeCount - 1 ELSE 0 END
            WHERE stat.studentServiceId = :studentServiceId AND stat.statDate = :statDate
            """)
    void decreaseLikeCount(
            @Param("studentServiceId") Long studentServiceId,
            @Param("statDate") LocalDate statDate
    );

    void deleteAllByStudentServiceId(Long studentServiceId);
}
