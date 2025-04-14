package kr.co.yournews.auth.dto;

public record TokenDto(
        String accessToken
) {
    public static TokenDto of(String accessToken) {
        return new TokenDto(accessToken);
    }
}
