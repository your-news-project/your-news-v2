package kr.co.yournews.apis.noticesummary.service;

import kr.co.yournews.apis.noticesummary.dto.NoticeSummaryDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.common.util.HashUtil;
import kr.co.yournews.domain.notification.entity.NoticeSummary;
import kr.co.yournews.domain.notification.exception.NotificationErrorType;
import kr.co.yournews.domain.notification.service.NoticeSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoticeSummaryQueryService {
    private final NoticeSummaryService noticeSummaryService;

    @Transactional(readOnly = true)
    public NoticeSummaryDto getNoticeSummaryByUrl(String url) {
        return NoticeSummaryDto.from(getNoticeSummary(url));
    }

    @Transactional(readOnly = true)
    public NoticeSummary getNoticeSummary(String url) {
        String urlHash = HashUtil.hash(url);

        // 해시 후보 중 정확히 일치하는 것 (해시 충돌을 대비해 List로 데이터 조회)
        Optional<NoticeSummary> noticeSummary = noticeSummaryService.readAllByUrlHash(urlHash).stream()
                .filter(ns -> url.equals(ns.getUrl()))
                .findFirst();

        // 데이터 있으면 반환, 없으면 URL로 조회
        return noticeSummary.or(() -> noticeSummaryService.readByUrl(url))
                .orElseThrow(() -> new CustomException(NotificationErrorType.SUMMARY_NOT_FOUND));
    }
}
