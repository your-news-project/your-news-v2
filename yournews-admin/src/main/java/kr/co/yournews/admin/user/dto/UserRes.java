package kr.co.yournews.admin.user.dto;

import kr.co.yournews.domain.news.dto.SubNewsQueryDto;
import kr.co.yournews.domain.news.type.KeywordType;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.type.OAuthPlatform;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserRes {

    public record Summary(
            Long id,
            String nickname,
            String email,
            boolean isBanned,
            LocalDateTime bannedAt
    ) {
        public static Summary from(User user) {
            return new Summary(
                    user.getId(), user.getNickname(), user.getEmail(),
                    user.isBanned(), user.getBannedAt()
            );
        }
    }

    public record Details(
            Long id,
            String username,
            String nickname,
            String email,
            List<String> subscriptions,
            List<String> keywords,
            boolean subStatus,
            boolean dailySubStatus,
            OAuthPlatform platform,
            boolean isBanned,
            String banReason,
            LocalDateTime bannedAt
    ) {
        public static Details from(User user, List<SubNewsQueryDto> subNewsList) {
            List<String> newsNames = new ArrayList<>();
            List<String> keywords = new ArrayList<>();

            for (SubNewsQueryDto dto : subNewsList) {
                newsNames.add(dto.newsName());
                if (dto.keywordTypes() != null) {
                    for (KeywordType keyword : dto.keywordTypes()) {
                        keywords.add(keyword.getLabel());
                    }
                }
            }

            return new Details(
                    user.getId(),
                    user.getUsername(),
                    user.getNickname(),
                    user.getEmail(),
                    newsNames,
                    keywords,
                    user.isSubStatus(),
                    user.isDailySubStatus(),
                    user.getPlatform(),
                    user.isBanned(),
                    user.getBanReason(),
                    user.getBannedAt()
            );
        }
    }
}
