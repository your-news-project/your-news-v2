package kr.co.yournews.infra.oauth.dto;

public record OAuthUserInfoRes(
        String id,
        String nickname,
        String email
) {
    public static OAuthUserInfoRes of(String id, String nickname, String email) {
        return new OAuthUserInfoRes(id, nickname, email);
    }
}
