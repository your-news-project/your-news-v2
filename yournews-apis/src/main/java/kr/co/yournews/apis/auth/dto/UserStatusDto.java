package kr.co.yournews.apis.auth.dto;

import kr.co.yournews.domain.user.entity.User;

public record UserStatusDto(
        User user,
        boolean isSignUp
) {
    public static UserStatusDto of(User user, boolean isSignUp) {
        return new UserStatusDto(user, isSignUp);
    }
}
