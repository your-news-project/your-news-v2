package kr.co.yournews.apis.noticesummary.service;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.notification.entity.NoticeSummary;
import kr.co.yournews.domain.notification.exception.NotificationErrorType;
import kr.co.yournews.domain.notification.service.NoticeSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeSummaryCommandService {
    private final NoticeSummaryService noticeSummaryService;

    /**
     * 요약된 공지 내용을 기존 데이터에 업데이트 하는 메서드
     *
     * @param urlHash : 조회를 위한 값 (인덱스 키)
     * @param summary : 요약 내용
     */
    @Transactional
    public void saveSummaryInfo(String urlHash, String summary) {
        NoticeSummary noticeSummary = noticeSummaryService.readByUrlHash(urlHash)
                .orElseThrow(() -> new CustomException(NotificationErrorType.SUMMARY_NOT_FOUND));

        noticeSummary.success(summary);
    }
}
