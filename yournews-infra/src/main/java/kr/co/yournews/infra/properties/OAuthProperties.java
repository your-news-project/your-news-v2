package kr.co.yournews.infra.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth2")
@Getter @Setter
public class OAuthProperties {
    private Platform kakao;
    private Platform naver;
    private ApplePlatform apple;

    @Getter @Setter
    public static class Platform {
        private String tokenUri;
        private String userInfoUri;
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String issuer;
    }

    @Getter @Setter
    public static class ApplePlatform extends Platform {
        private String teamId;
        private String keyId;
        private String privateKey;
    }
}
