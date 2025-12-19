package kr.co.yournews.infra.iap.config;

import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.model.Environment;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Configuration
public class AppleIapConfig {

    @Bean
    public AppStoreServerAPIClient appStoreServerAPIClient(AppleIapProperties properties) {
        Environment environment = "PRODUCTION".equalsIgnoreCase(properties.getEnvironment())
                ? Environment.PRODUCTION
                : Environment.SANDBOX;

        return new AppStoreServerAPIClient(
                properties.getPrivateKey(),
                properties.getKeyId(),
                properties.getIssuerId(),
                properties.getBundleId(),
                environment
        );
    }

    @Bean
    public SignedDataVerifier signedDataVerifier(AppleIapProperties properties) throws IOException {
        Environment environment = "PRODUCTION".equalsIgnoreCase(properties.getEnvironment())
                ? Environment.PRODUCTION
                : Environment.SANDBOX;

        InputStream rootCaInputStream =
                new ClassPathResource("cert/AppleRootCA-G3.cer").getInputStream();

        Set<InputStream> rootCAs = Set.of(rootCaInputStream);

        return new SignedDataVerifier(
                rootCAs,
                properties.getBundleId(),
                properties.getAppleId(),
                environment,
                true
        );
    }
}
