package kr.co.yournews.infra.iap.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

@Configuration
public class GoogleIapConfig {

    @Value("${google-iap.service-account-key}")
    private String serviceAccountKey;

    @Bean
    public AndroidPublisher androidPublisher() {
        try (InputStream is = new ClassPathResource(serviceAccountKey).getInputStream();) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(is)
                    .createScoped(List.of(AndroidPublisherScopes.ANDROIDPUBLISHER));

            HttpRequestInitializer requestInitializer =
                    new HttpCredentialsAdapter(credentials);

            return new AndroidPublisher.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    requestInitializer
            )
                    .setApplicationName("your-news")
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException("Google IAP AndroidPublisher init failed", e);
        }
    }
}
