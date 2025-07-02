package kr.co.yournews.infra.oauth.basic.naver;

import com.fasterxml.jackson.databind.JsonNode;
import kr.co.yournews.infra.oauth.basic.BaseOAuthClient;
import kr.co.yournews.infra.oauth.basic.OAuthHttpClient;
import kr.co.yournews.infra.oauth.dto.OAuthUserInfoRes;
import kr.co.yournews.infra.properties.OAuthProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class NaverOAuthClient extends BaseOAuthClient {

    public NaverOAuthClient(
            OAuthHttpClient httpClient,
            OAuthProperties properties
    ) {
        super(httpClient, properties.getNaver());
    }

    @Override
    protected MultiValueMap<String, String> getAccessTokenParams(OAuthProperties.Platform properties, String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", properties.getClientId());
        params.add("client_secret", properties.getClientSecret());
        params.add("redirect_uri", properties.getRedirectUri());
        params.add("code", code);
        return params;
    }

    @Override
    protected OAuthUserInfoRes parseUserInfo(JsonNode rootNode) {
        String id = rootNode.path("response").get("id").asText();
        String email = rootNode.path("response").get("email").asText();
        return OAuthUserInfoRes.of(id, email);
    }
}
