package kr.co.yournews.infra.oauth.oidc;

import kr.co.yournews.infra.oauth.dto.oidc.OidcDecodePayload;
import kr.co.yournews.infra.oauth.dto.oidc.OidcPublicKey;
import kr.co.yournews.infra.oauth.dto.oidc.OidcPublicKeysResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthOidcHelper {
    private final JwtOidcProvider jwtOidcProvider;

    /**
     * ID Token에서 사용자 정보를 추출하는 메서드
     * <p>
     * 1. ID Token에서 kid 값을 추출
     * 2. 공개키 목록에서 해당 kid에 맞는 키를 탐색
     * 3. 해당 공개키(n, e)로 서명을 검증하고 Payload를 반환
     *
     * @param idToken  : id token
     * @param iss      : 기대하는 issuer
     * @param aud      : 기대하는 audience (clientId)
     * @param response : OIDC 공개키 목록 (JWK Set)
     * @return : 사용자 정보가 담긴 Payload
     */
    public OidcDecodePayload getPayloadFromIdToken(
            String idToken, String iss, String aud, OidcPublicKeysResponse response
    ) {
        String kid = getKidFromUnsignedIdToken(idToken, iss, aud);

        OidcPublicKey oidcPublicKeyDto =
                response.keys().stream()
                        .filter(o -> o.kid().equals(kid))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No matching public key found"));

        return jwtOidcProvider.getOIDCTokenBody(
                idToken, oidcPublicKeyDto.n(), oidcPublicKeyDto.e()
        );
    }

    /**
     * 공개키를 찾기 위한 key id(kid)를 추출하는 메서드
     *
     * @param idToken : id token
     * @param iss     : 기대하는 issuer
     * @param aud     : 기대하는 audience (clientId)
     * @return kid 값
     */
    private String getKidFromUnsignedIdToken(String idToken, String iss, String aud) {
        return jwtOidcProvider.getKidFromUnsignedTokenHeader(idToken, iss, aud);
    }
}
