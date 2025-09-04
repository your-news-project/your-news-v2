package kr.co.yournews.domain.notification.repository;

import kr.co.yournews.domain.notification.entity.NoticeSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeSummaryRepository extends JpaRepository<NoticeSummary, Long>, CustomNoticeSummaryRepository {
    Optional<NoticeSummary> findByUrlHash(String urlHash);
}
