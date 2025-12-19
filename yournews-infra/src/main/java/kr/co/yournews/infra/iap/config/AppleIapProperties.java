package kr.co.yournews.infra.iap.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "apple-iap")
@Getter @Setter
public class AppleIapProperties {
    private String issuerId;
    private String keyId;
    private String bundleId;
    private String privateKey;
    private Long appleId;
}
