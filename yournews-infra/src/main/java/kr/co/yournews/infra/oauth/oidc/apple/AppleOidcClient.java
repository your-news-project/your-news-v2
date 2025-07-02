package kr.co.yournews.infra.oauth.oidc.apple;

import kr.co.yournews.infra.oauth.oidc.key.AppleOidcKeyClient;
import kr.co.yournews.infra.oauth.oidc.BaseOAuthOidcClient;
import kr.co.yournews.infra.oauth.oidc.OAuthOidcHelper;
import kr.co.yournews.infra.oauth.oidc.OAuthOidcHttpClient;
import kr.co.yournews.infra.properties.OAuthProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class AppleOidcClient extends BaseOAuthOidcClient {
    private final AppleClientSecretProvider clientSecretProvider;

    public AppleOidcClient(
            OAuthOidcHttpClient httpClient,
            OAuthProperties properties,
            AppleOidcKeyClient oidcClient,
            OAuthOidcHelper oAuthOidcHelper,
            AppleClientSecretProvider clientSecretProvider
    ) {
        super(httpClient, properties.getApple(), oidcClient, oAuthOidcHelper);
        this.clientSecretProvider = clientSecretProvider;
    }

    @Override
    protected MultiValueMap<String, String> getTokenParams(OAuthProperties.Platform properties, String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", properties.getClientId());
        params.add("client_secret", clientSecretProvider.createClientSecret(properties));
        params.add("redirect_uri", properties.getRedirectUri());
        params.add("code", code);
        return params;
    }
}
