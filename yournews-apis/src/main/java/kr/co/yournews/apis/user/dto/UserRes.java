package kr.co.yournews.apis.user.dto;

import kr.co.yournews.domain.news.dto.SubNewsQueryDto;
import kr.co.yournews.domain.news.type.KeywordType;
import kr.co.yournews.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;

public record UserRes(
        Long id,
        String username,
        String nickname,
        String email,
        List<String> subscriptions,
        List<String> keywords,
        boolean subStatus,
        boolean dailySubStatus
) {
    public static UserRes from(User user, List<SubNewsQueryDto> subNewsList) {
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

        return new UserRes(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                newsNames,
                keywords,
                user.isSubStatus(),
                user.isDailySubStatus()
        );
    }
}
