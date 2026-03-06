package kr.co.yournews.domain.notification.repository.messageprocesssummary;

import kr.co.yournews.domain.notification.entity.MessageProcessHourlySummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageProcessHourlySummaryRepository extends JpaRepository<MessageProcessHourlySummary, Long> {
}
