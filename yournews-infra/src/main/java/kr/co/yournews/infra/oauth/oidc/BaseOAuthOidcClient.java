package kr.co.yournews.infra.oauth.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.common.response.error.type.GlobalErrorType;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.infra.oauth.OAuthClient;
import kr.co.yournews.infra.oauth.dto.OAuthUserInfoRes;
import kr.co.yournews.infra.oauth.dto.oidc.OidcDecodePayload;
import kr.co.yournews.infra.oauth.dto.oidc.OidcPublicKeysResponse;
import kr.co.yournews.infra.oauth.oidc.key.OauthOidcKeyClient;
import kr.co.yournews.infra.properties.OAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.util.MultiValueMap;

@RequiredArgsConstructor
public abstract class BaseOAuthOidcClient implements OAuthClient {
    private final OAuthOidcHttpClient httpClient;
    private final OAuthProperties.Platform properties;
    private final OauthOidcKeyClient oidcClient;
    private final OAuthOidcHelper oAuthOidcHelper;

    /**
     * OIDC 기반 OAuth 인증 처리 메서드
     * <p>
     * 1. 인가 코드(code)를 이용해 토큰 발급 API를 호출
     * 2. 응답에서 id_token을 추출
     * 3. JWK 공개키 세트(keySet)를 가져와 id_token의 서명 및 payload를 검증
     * 4. payload에서 사용자 식별자(sub) 및 이메일 정보를 추출하여 반환
     *
     * @param code : 플랫폼 인가 코드
     * @return : 사용자 식별자 및 이메일 정보
     */
    @Override
    public final OAuthUserInfoRes authenticate(String code) {
        String tokenJson = httpClient.getTokenResponse(properties.getTokenUri(), getTokenParams(properties, code));
        String idToken = extractIdToken(tokenJson);

        OidcPublicKeysResponse keySet = oidcClient.getOidcPublicKey();
        OidcDecodePayload payload = oAuthOidcHelper.getPayloadFromIdToken(
                idToken,
                properties.getIssuer(),
                properties.getClientId(),
                keySet
        );

        return OAuthUserInfoRes.of(payload.sub(), payload.email());
    }

    /**
     * 플랫폼의 token을 가져오기 위한 파라미터 생성 메서드
     *
     * @param code : 플랫폼 인가 코드
     * @return : 파라미터 값
     */
    protected abstract MultiValueMap<String, String> getTokenParams(
            OAuthProperties.Platform properties,
            String code
    );

    /**
     * 받아온 JSON 응답에서 id token 추출하는 메서드
     *
     * @param json : 추출할 json
     * @return : id token
     */
    private String extractIdToken(String json) {
        try {
            JsonNode root = new ObjectMapper().readTree(json);
            return root.path("id_token").asText(null);
        } catch (Exception e) {
            throw new CustomException(GlobalErrorType.INTERNAL_SERVER_ERROR);
        }
    }
}
