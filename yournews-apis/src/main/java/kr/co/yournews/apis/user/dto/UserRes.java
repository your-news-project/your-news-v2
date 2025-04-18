package kr.co.yournews.apis.user.dto;

import kr.co.yournews.domain.user.entity.User;

public record UserRes(
        Long id,
        String username,
        String nickname,
        String email
) {
    public static UserRes from(User user) {
        return new UserRes(user.getId(), user.getUsername(), user.getNickname(), user.getEmail());
    }
}
