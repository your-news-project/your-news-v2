package kr.co.yournews.domain.studentservice.repository;

import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import kr.co.yournews.domain.studentservice.type.StudentServiceContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.LockModeType;

public interface StudentServiceRepository extends JpaRepository<StudentService, Long> {
    Page<StudentService> findAllByStatus(StudentServiceStatus status, Pageable pageable);
    Page<StudentService> findAllByStatusAndContentType(
            StudentServiceStatus status,
            StudentServiceContentType contentType,
            Pageable pageable
    );
    Page<StudentService> findAllByStatusAndContentTypeOrStatusAndContentTypeIsNull(
            StudentServiceStatus status,
            StudentServiceContentType contentType,
            StudentServiceStatus nullContentTypeStatus,
            Pageable pageable
    );
    Page<StudentService> findAllByUser_Id(Long userId, Pageable pageable);

    @Query(value = """
            SELECT service.*
            FROM student_service service
            JOIN (
                SELECT student_service_id,
                       SUM(like_count * 5 + click_count * 3 + view_count) AS popularity_score
                FROM student_service_daily_stat
                WHERE stat_date >= :startDate
                GROUP BY student_service_id
            ) stat ON stat.student_service_id = service.id
            WHERE service.status = 'APPROVED'
            ORDER BY stat.popularity_score DESC,
                     COALESCE(service.approved_at, service.created_at) DESC
            LIMIT 3
            """, nativeQuery = true)
    List<StudentService> findTop3PopularSince(@Param("startDate") LocalDate startDate);

    @Query("""
            SELECT service FROM student_service service
            WHERE service.status = :status
            ORDER BY COALESCE(service.approvedAt, service.createdAt) DESC
            """)
    List<StudentService> findLatest(
            @Param("status") StudentServiceStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT service FROM student_service service
            WHERE service.status = :status
              AND COALESCE(service.approvedAt, service.createdAt) >= :startAt
            ORDER BY COALESCE(service.approvedAt, service.createdAt) DESC
            """)
    List<StudentService> findLatestSince(
            @Param("status") StudentServiceStatus status,
            @Param("startAt") LocalDateTime startAt,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT service FROM student_service service
            WHERE service.status = :status
            ORDER BY service.id ASC
            """)
    List<StudentService> findAllApprovedForPromotionUpdate(
            @Param("status") StudentServiceStatus status
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE student_service "
            + "SET click_count = click_count + 1 "
            + "WHERE id = :studentServiceId", nativeQuery = true)
    int increaseClickCount(@Param("studentServiceId") Long studentServiceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE student_service "
            + "SET view_count = view_count + 1 "
            + "WHERE id = :studentServiceId", nativeQuery = true)
    int increaseViewCount(@Param("studentServiceId") Long studentServiceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE student_service "
            + "SET like_count = like_count + 1 "
            + "WHERE id = :studentServiceId", nativeQuery = true)
    int increaseLikeCount(@Param("studentServiceId") Long studentServiceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE student_service "
            + "SET like_count = GREATEST(like_count - 1, 0) "
            + "WHERE id = :studentServiceId", nativeQuery = true)
    int decreaseLikeCount(@Param("studentServiceId") Long studentServiceId);
}
