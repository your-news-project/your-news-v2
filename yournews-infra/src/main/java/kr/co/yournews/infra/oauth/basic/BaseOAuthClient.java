package kr.co.yournews.infra.oauth.basic;

import com.fasterxml.jackson.databind.JsonNode;
import kr.co.yournews.infra.oauth.OAuthClient;
import kr.co.yournews.infra.oauth.dto.OAuthUserInfoRes;
import kr.co.yournews.infra.properties.OAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;

@RequiredArgsConstructor
public abstract class BaseOAuthClient implements OAuthClient {
    private final OAuthHttpClient httpClient;
    private final OAuthProperties.Platform properties;

    /**
     * OAuth2 기반 로그인 처리 메서드
     * <p>
     * 1. 인가 코드(code)를 이용해 access token을 발급
     * 2. access token을 사용하여 사용자 정보 API에 요청
     * 3. 응답 데이터를 파싱해 사용자 식별자 및 이메일 정보를 반환
     *
     * @param code : 플랫폼 인가 코드
     * @return : 사용자 식별자 및 이메일 정보
     */
    @Override
    public OAuthUserInfoRes authenticate(String code) {
        String accessToken =
                httpClient.getAccessToken(properties.getTokenUri(), getAccessTokenParams(properties, code));

        JsonNode userInfoNode = httpClient.getUserInfo(properties.getUserInfoUri(), accessToken);
        return parseUserInfo(userInfoNode);
    }

    /**
     * 플랫폼의 access token을 가져오기 위한 파라미터 생성 메서드.
     *
     * @param code : 플랫폼 인가 코드
     * @return : 파라미터 값
     */
    protected abstract MultiValueMap<String, String> getAccessTokenParams(
            OAuthProperties.Platform properties,
            String code
    );

    /**
     * 플랫폼의 사용자 정보를 추출하는 메서드.
     *
     * @param rootNode : 플랫폼의 사용자 정보 json
     * @return : 추출한 사용자 정보
     */
    protected abstract OAuthUserInfoRes parseUserInfo(JsonNode rootNode);
}
