package kr.co.yournews.infra.oauth.dto.oidc;

/**
 * OIDC ID Token 디코딩 결과 (JWT의 Payload 정보)
 */
public record OidcDecodePayload(
        String iss,     // 토큰 발급자
        String aud,     // 대상자 (일반적으로 client_id)
        String sub,     // OAuth 제공자의 유저 고유 ID
        String email
) {
}
