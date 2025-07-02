package kr.co.yournews.infra.oauth.dto.oidc;

/**
 * OIDC 공개키 (JWK) 객체
 */
public record OidcPublicKey(
        String kid,   // Key ID (JWT 헤더에 포함됨, 어떤 키로 서명했는지 식별)
        String alg,   // 알고리즘 (ex. RS256)
        String use,   // 키 용도 (ex. "sig" -> 서명용)
        String n,     // RSA 공개키의 modulus (Base64URL 인코딩된 값)
        String e      // RSA 공개키의 exponent (Base64URL 인코딩된 값)
) {
}
