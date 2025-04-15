package kr.co.yournews.infra.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import kr.co.yournews.infra.oauth.dto.OAuthUserInfoRes;
import kr.co.yournews.infra.properties.OAuthProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class KakaoOAuthClient extends OAuthClient {

    public KakaoOAuthClient(OAuthHttpClient oAuthService, OAuthProperties oAuthProperties) {
        super(oAuthService, oAuthProperties.getKakao());
    }

    @Override
    protected MultiValueMap<String, String> getAccessTokenParams(OAuthProperties.Platform kakaoProperties, String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoProperties.getClientId());
        params.add("client_secret", kakaoProperties.getClientSecret());
        params.add("redirect_uri", kakaoProperties.getRedirectUri());
        params.add("code", code);
        return params;
    }

    @Override
    protected OAuthUserInfoRes parseUserInfo(JsonNode rootNode) {
        String id = rootNode.path("id").asText();
        String nickname = rootNode.path("kakao_account").path("profile").get("nickname").asText();
        String email = rootNode.path("kakao_account").path("email").asText();
        return OAuthUserInfoRes.of(id, nickname, email);
    }
}
