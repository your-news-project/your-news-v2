package kr.co.yournews.infra.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import kr.co.yournews.infra.oauth.dto.OAuthUserInfoRes;
import kr.co.yournews.infra.properties.OAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;

@RequiredArgsConstructor
public abstract class OAuthClient {
    private final OAuthHttpClient oAuthHttpClient;
    private final OAuthProperties.Platform oAuthProperties;

    /**
     * 플랫폼의 access token을 가져오기 위한 파라미터 생성 메서드.
     *
     * @param code : 플랫폼 인증 코드
     * @return : 파라미터 값
     */
    protected abstract MultiValueMap<String, String> getAccessTokenParams(
            OAuthProperties.Platform oAuthProperties,
            String code
    );

    /**
     * 플랫폼의 사용자 정보를 추출하는 메서드.
     *
     * @param rootNode : 플랫폼의 사용자 정보 json
     * @return : 추출한 사용자 정보
     */
    protected abstract OAuthUserInfoRes parseUserInfo(JsonNode rootNode);

    public OAuthUserInfoRes fetchUserInfoFromPlatform(String code) {
        String accessToken =
                oAuthHttpClient.getAccessToken(getTokenUri(), getAccessTokenParams(oAuthProperties, code));

        JsonNode userInfoNode = oAuthHttpClient.getUserInfo(getUserInfoUri(), accessToken);
        return parseUserInfo(userInfoNode);
    }

    /**
     * 플랫폼의 access token을 받아오기 위한 uri를 가져오는 메서드.
     *
     * @return : token uri
     */
    private String getTokenUri() {
        return oAuthProperties.getTokenUri();
    }

    /**
     * 플랫폼의 사용자 정보를 가져오기 위해 uri를 가져오는 메서드.
     *
     * @return : userInfo uri
     */
    private String getUserInfoUri() {
        return oAuthProperties.getUserInfoUri();
    }
}
