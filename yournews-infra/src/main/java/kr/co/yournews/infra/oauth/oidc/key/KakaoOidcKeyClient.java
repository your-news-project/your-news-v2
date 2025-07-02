package kr.co.yournews.infra.oauth.oidc.key;

import kr.co.yournews.infra.oauth.dto.oidc.OidcPublicKeysResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "KakaoOidcKeyClient",
        url = "https://kauth.kakao.com"
)
public interface KakaoOidcKeyClient extends OauthOidcKeyClient {
    @Override
    @GetMapping("/.well-known/jwks.json")
    OidcPublicKeysResponse getOidcPublicKey();
}

