package kr.co.yournews.apis.auth.dto;

import kr.co.yournews.auth.dto.TokenDto;

public record OAuthTokenDto(
        TokenDto tokenDto,
        boolean isSignUp
) {
    public static OAuthTokenDto of(TokenDto tokenDto, boolean isSignUp) {
        return new OAuthTokenDto(tokenDto, isSignUp);
    }
}
