package kr.co.yournews.apis.crawling.strategy.dto;

import kr.co.yournews.domain.menu.type.MenuType;
import lombok.Builder;

@Builder
public record MenuFileDownloadReq(
        String originalFileName,
        String fileUrl,
        String refererUrl,
        MenuType menuType
) {
}
