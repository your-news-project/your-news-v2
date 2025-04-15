package kr.co.yournews.infra.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import kr.co.yournews.infra.oauth.dto.OAuthUserInfoRes;
import kr.co.yournews.infra.properties.OAuthProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class NaverOAuthClient extends OAuthClient {

    public NaverOAuthClient(OAuthHttpClient oAuthService, OAuthProperties oAuthProperties) {
        super(oAuthService, oAuthProperties.getNaver());
    }

    @Override
    protected MultiValueMap<String, String> getAccessTokenParams(OAuthProperties.Platform naverProperties, String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverProperties.getClientId());
        params.add("client_secret", naverProperties.getClientSecret());
        params.add("redirect_uri", naverProperties.getRedirectUri());
        params.add("code", code);
        return params;
    }

    @Override
    protected OAuthUserInfoRes parseUserInfo(JsonNode rootNode) {
        String id = rootNode.path("response").get("id").asText();
        String nickname = rootNode.path("response").get("nickname").asText();
        String email = rootNode.path("response").get("email").asText();
        return OAuthUserInfoRes.of(id, nickname, email);
    }
}
