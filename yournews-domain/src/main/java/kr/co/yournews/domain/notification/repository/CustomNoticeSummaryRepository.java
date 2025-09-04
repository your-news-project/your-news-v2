package kr.co.yournews.domain.notification.repository;

import kr.co.yournews.domain.notification.entity.NoticeSummary;

import java.util.List;

public interface CustomNoticeSummaryRepository {
    void saveAllInBatch(List<NoticeSummary> noticeSummaries);
}
