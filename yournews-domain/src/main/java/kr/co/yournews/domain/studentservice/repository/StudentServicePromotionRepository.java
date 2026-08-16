package kr.co.yournews.domain.studentservice.repository;

import jakarta.persistence.LockModeType;
import kr.co.yournews.domain.studentservice.entity.StudentServicePromotion;
import kr.co.yournews.domain.studentservice.type.StudentServicePromotionStatus;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StudentServicePromotionRepository extends JpaRepository<StudentServicePromotion, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT promotion FROM student_service_promotion promotion
            JOIN FETCH promotion.studentService service
            WHERE promotion.status IN :statuses
            ORDER BY promotion.queuedAt ASC, promotion.id ASC
            """)
    List<StudentServicePromotion> findAllOpenForUpdate(
            @Param("statuses") Collection<StudentServicePromotionStatus> statuses
    );

    @Query("""
            SELECT promotion FROM student_service_promotion promotion
            JOIN FETCH promotion.studentService service
            WHERE promotion.status = :status
              AND promotion.endsAt > :now
              AND service.status = :serviceStatus
            ORDER BY promotion.startedAt ASC, promotion.id ASC
            """)
    List<StudentServicePromotion> findTop5Active(
            @Param("status") StudentServicePromotionStatus status,
            @Param("serviceStatus") StudentServiceStatus serviceStatus,
            @Param("now") LocalDateTime now,
            org.springframework.data.domain.Pageable pageable
    );

    List<StudentServicePromotion> findAllByStudentService_IdInAndStatusIn(
            Collection<Long> studentServiceIds,
            Collection<StudentServicePromotionStatus> statuses
    );
}
