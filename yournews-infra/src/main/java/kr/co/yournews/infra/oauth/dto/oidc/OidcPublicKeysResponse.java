package kr.co.yournews.infra.oauth.dto.oidc;

import java.util.List;

/**
 * OIDC 공개키 목록 응답 객체 (JWK Set)
 */
public record OidcPublicKeysResponse(
        List<OidcPublicKey> keys
) {
}
