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

    @Bean("appStoreClientProd")
    public AppStoreServerAPIClient appStoreClientProd(AppleIapProperties properties) {
        return new AppStoreServerAPIClient(
                properties.getPrivateKey(),
                properties.getKeyId(),
                properties.getIssuerId(),
                properties.getBundleId(),
                Environment.PRODUCTION
        );
    }

    @Bean("appStoreClientSandbox")
    public AppStoreServerAPIClient appStoreClientSandbox(AppleIapProperties properties) {
        return new AppStoreServerAPIClient(
                properties.getPrivateKey(),
                properties.getKeyId(),
                properties.getIssuerId(),
                properties.getBundleId(),
                Environment.SANDBOX
        );
    }

    @Bean("signedVerifierProd")
    public SignedDataVerifier signedVerifierProd(AppleIapProperties properties) throws IOException {
        return buildVerifier(properties, Environment.PRODUCTION);
    }

    @Bean("signedVerifierSandbox")
    public SignedDataVerifier signedVerifierSandbox(AppleIapProperties properties) throws IOException {
        return buildVerifier(properties, Environment.SANDBOX);
    }

    private SignedDataVerifier buildVerifier(AppleIapProperties properties, Environment env) throws IOException {
        InputStream rootCaInputStream =
                new ClassPathResource("cert/AppleRootCA-G3.cer").getInputStream();

        Set<InputStream> rootCAs = Set.of(rootCaInputStream);

        return new SignedDataVerifier(
                rootCAs,
                properties.getBundleId(),
                properties.getAppleId(),
                env,
                true
        );
    }
}
