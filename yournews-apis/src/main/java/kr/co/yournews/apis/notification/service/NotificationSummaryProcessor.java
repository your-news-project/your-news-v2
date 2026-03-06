package kr.co.yournews.apis.notification.service;

import kr.co.yournews.domain.notification.entity.MessageProcessHourlySummary;
import kr.co.yournews.domain.notification.entity.NotificationOutboxDailySummary;
import kr.co.yournews.domain.notification.repository.messageprocess.MessageProcessHourlyAggregation;
import kr.co.yournews.domain.notification.repository.outbox.NotificationOutboxDailyAggregation;
import kr.co.yournews.domain.notification.service.MessageProcessHourlySummaryService;
import kr.co.yournews.domain.notification.service.MessageProcessService;
import kr.co.yournews.domain.notification.service.NotificationOutboxDailySummaryService;
import kr.co.yournews.domain.notification.service.NotificationOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 메시지 발행 및 처리 데이터 집계 서비스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSummaryProcessor {
    private final MessageProcessService messageProcessService;
    private final MessageProcessHourlySummaryService messageProcessHourlySummaryService;
    private final NotificationOutboxService notificationOutboxService;
    private final NotificationOutboxDailySummaryService notificationOutboxDailySummaryService;

    @Transactional
    public void summarizePreviousDay() {
        LocalDateTime startDateTime = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime endDateTime = startDateTime.plusDays(1);

        summarizeMessageProcess(startDateTime, endDateTime);
        summarizeNotificationOutbox(startDateTime, endDateTime);
    }

    /**
     * 전일 created_at 기준 시간대별 메시지 처리 결과 저장 메서드
     */
    private void summarizeMessageProcess(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<MessageProcessHourlyAggregation> aggregations = messageProcessService.readHourlyAggregations(startDateTime, endDateTime);
        List<MessageProcessHourlySummary> summaries = new ArrayList<>();

        for (MessageProcessHourlyAggregation aggregation : aggregations) {
            summaries.add(
                    MessageProcessHourlySummary.builder()
                            .summaryStartedAt(aggregation.getSummaryStartedAt())
                            .totalCount((int) aggregation.getTotalCount())
                            .successCount((int) aggregation.getSuccessCount())
                            .failureCount((int) aggregation.getFailureCount())
                            .retryCount((int) aggregation.getRetryCount())
                            .dlqFailedCount((int) aggregation.getDlqFailedCount())
                            .firstCreatedAt(aggregation.getFirstCreatedAt())
                            .lastCompletedAt(aggregation.getLastCompletedAt())
                            .processingDurationSeconds(aggregation.getProcessingDurationSeconds())
                            .build()
            );
        }

        messageProcessHourlySummaryService.saveAll(summaries);
        log.info("[메시지 처리 시간대별 집계 완료] start: {}, end: {}, hourCount: {}", startDateTime, endDateTime, aggregations.size());
    }

    /**
     * 아웃박스는 전일 생성 건을 기준으로 일간 발행 집계 저장 메서드
     */
    private void summarizeNotificationOutbox(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Optional<NotificationOutboxDailyAggregation> aggregation = notificationOutboxService.readDailyAggregation(startDateTime, endDateTime);
        if (aggregation.isEmpty()) {
            log.info("[아웃박스 일간 집계 스킵] date: {}, reason: no_data", startDateTime.toLocalDate());
            return;
        }

        NotificationOutboxDailyAggregation result = aggregation.get();
        notificationOutboxDailySummaryService.save(
                NotificationOutboxDailySummary.builder()
                        .summaryDate(result.getSummaryDate())
                        .publishCount((int) result.getPublishCount())
                        .successCount((int) result.getSuccessCount())
                        .failureCount((int) result.getFailureCount())
                        .retryCount((int) result.getRetryCount())
                        .build()
        );

        log.info("[아웃박스 일간 집계 완료] date: {}", result.getSummaryDate());
    }
}
