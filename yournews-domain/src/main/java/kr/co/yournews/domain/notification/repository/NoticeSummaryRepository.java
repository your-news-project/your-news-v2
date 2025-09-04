package kr.co.yournews.domain.notification.repository;

import kr.co.yournews.domain.notification.entity.NoticeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NoticeSummaryRepository extends JpaRepository<NoticeSummary, Long>, CustomNoticeSummaryRepository {
    List<NoticeSummary> findAllByUrlHash(String urlHash);
    Optional<NoticeSummary> findByUrl(String url);

    @Modifying
    @Query("DELETE FROM notice_summary n WHERE n.createdAt < :dateTime")
    void deleteByDateTimeBefore(@Param("dateTime") LocalDateTime dateTime);
}
