package kr.co.yournews.apis.user.dto;

import kr.co.yournews.domain.news.dto.SubNewsQueryDto;
import kr.co.yournews.domain.news.type.KeywordType;
import kr.co.yournews.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;

public class UserRes {

    public record Info(
            Long id,
            String username,
            String nickname,
            String email,
            List<String> subscriptions,
            List<String> keywords,
            boolean subStatus,
            boolean dailySubStatus,
            boolean calendarSubStatus,

            boolean isOauth
    ) {
        public static Info from(User user, List<SubNewsQueryDto> subNewsList) {
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

            return new Info(
                    user.getId(),
                    user.getUsername(),
                    user.getNickname(),
                    user.getEmail(),
                    newsNames,
                    keywords,
                    user.isSubStatus(),
                    user.isDailySubStatus(),
                    user.isCalendarSubStatus(),
                    user.isOauthUser()
            );
        }
    }

    public record NotificationStatus(
            boolean subStatus,
            boolean dailySubStatus,
            boolean calendarSubStatus
    ) {
        public static NotificationStatus from(User user) {
            return new NotificationStatus(
                    user.isSubStatus(),
                    user.isDailySubStatus(),
                    user.isCalendarSubStatus()
            );
        }
    }
}