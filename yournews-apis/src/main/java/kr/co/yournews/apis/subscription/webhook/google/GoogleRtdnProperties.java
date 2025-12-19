package kr.co.yournews.apis.subscription.webhook.google;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "google.rtdn")
@Getter @Setter
public class GoogleRtdnProperties {
    private String email;
    private String audience;
    private String jwksUri;
}
