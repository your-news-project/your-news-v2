package kr.co.yournews.infra.oauth.oidc.key;

import kr.co.yournews.infra.oauth.dto.oidc.OidcPublicKeysResponse;

public interface OauthOidcKeyClient {
    OidcPublicKeysResponse getOidcPublicKey();
}
