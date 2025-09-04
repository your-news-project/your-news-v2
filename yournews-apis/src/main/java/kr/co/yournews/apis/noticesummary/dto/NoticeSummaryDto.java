package kr.co.yournews.apis.noticesummary.dto;

import kr.co.yournews.domain.notification.entity.NoticeSummary;
import kr.co.yournews.domain.notification.type.SummaryStatus;

public record NoticeSummaryDto(
        SummaryStatus status,
        String summary
) {
    public static NoticeSummaryDto from(NoticeSummary noticeSummary) {
        return new NoticeSummaryDto(noticeSummary.getStatus(), noticeSummary.getSummary());
    }
}
