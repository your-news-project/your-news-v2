package kr.co.yournews.apis.noticesummary.service;

import kr.co.yournews.domain.notification.entity.NoticeSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeSummaryCommandService {
    private final NoticeSummaryQueryService noticeSummaryQueryService;

    /**
     * 요약된 공지 내용을 기존 데이터에 업데이트 하는 메서드
     *
     * @param url     : 조회를 위한 url
     * @param summary : 요약 내용
     */
    @Transactional
    public void saveSummaryInfo(String url, String summary) {
        NoticeSummary noticeSummary = noticeSummaryQueryService.getNoticeSummary(url);

        noticeSummary.success(summary);
    }
}
