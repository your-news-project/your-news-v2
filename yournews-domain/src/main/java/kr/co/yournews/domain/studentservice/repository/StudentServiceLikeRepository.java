package kr.co.yournews.domain.studentservice.repository;

import kr.co.yournews.domain.studentservice.entity.StudentServiceLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentServiceLikeRepository extends JpaRepository<StudentServiceLike, Long> {
    boolean existsByUser_IdAndStudentService_Id(Long userId, Long studentServiceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM student_service_like "
            + "WHERE user_id = :userId AND student_service_id = :studentServiceId", nativeQuery = true)
    int deleteByUserIdAndStudentServiceId(
            @Param("userId") Long userId,
            @Param("studentServiceId") Long studentServiceId
    );
}
