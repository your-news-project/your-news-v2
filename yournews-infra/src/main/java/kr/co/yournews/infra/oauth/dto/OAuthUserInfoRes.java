package kr.co.yournews.infra.oauth.dto;

public record OAuthUserInfoRes(
        String id,
        String email
) {
    public static OAuthUserInfoRes of(String id, String email) {
        return new OAuthUserInfoRes(id, email);
    }
}
