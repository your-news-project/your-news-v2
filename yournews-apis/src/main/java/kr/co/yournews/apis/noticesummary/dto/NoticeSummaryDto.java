package kr.co.yournews.apis.noticesummary.dto;

import kr.co.yournews.domain.notification.entity.NoticeSummary;

public record NoticeSummaryDto(
        String urlHash,
        String summary
) {
    public static NoticeSummaryDto from(NoticeSummary noticeSummary) {
        return new NoticeSummaryDto(noticeSummary.getUrlHash(), noticeSummary.getSummary());
    }
}
