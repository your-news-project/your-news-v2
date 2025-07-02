package kr.co.yournews.infra.oauth.oidc.key;

import kr.co.yournews.infra.oauth.dto.oidc.OidcPublicKeysResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "AppleOidcKeyClient",
        url = "https://appleid.apple.com"
)
public interface AppleOidcKeyClient extends OauthOidcKeyClient {
    @Override
    @GetMapping("/auth/keys")
    OidcPublicKeysResponse getOidcPublicKey();
}
