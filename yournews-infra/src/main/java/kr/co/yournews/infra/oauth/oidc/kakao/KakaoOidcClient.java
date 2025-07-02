package kr.co.yournews.infra.oauth.oidc.kakao;

import kr.co.yournews.infra.oauth.oidc.OAuthOidcHelper;
import kr.co.yournews.infra.oauth.oidc.BaseOAuthOidcClient;
import kr.co.yournews.infra.oauth.oidc.OAuthOidcHttpClient;
import kr.co.yournews.infra.oauth.oidc.key.KakaoOidcKeyClient;
import kr.co.yournews.infra.properties.OAuthProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class KakaoOidcClient extends BaseOAuthOidcClient {

    public KakaoOidcClient(
            OAuthOidcHttpClient httpClient,
            OAuthProperties properties,
            KakaoOidcKeyClient oidcClient,
            OAuthOidcHelper oAuthOidcHelper
    ) {
        super(httpClient, properties.getKakao(), oidcClient, oAuthOidcHelper);
    }

    @Override
    protected MultiValueMap<String, String> getTokenParams(OAuthProperties.Platform properties, String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", properties.getClientId());
        params.add("client_secret", properties.getClientSecret());
        params.add("redirect_uri", properties.getRedirectUri());
        params.add("code", code);
        return params;
    }
}
