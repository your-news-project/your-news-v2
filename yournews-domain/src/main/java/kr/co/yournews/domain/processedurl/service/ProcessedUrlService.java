package kr.co.yournews.domain.processedurl.service;

import kr.co.yournews.domain.processedurl.spi.ProcessedUrlStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ProcessedUrlService {
    private final ProcessedUrlStore processedUrlStore;

    private static final String PROCESSED_URL_PREFIX = "processed-url::";

    public void save(String url, long ttl) {
        String key = PROCESSED_URL_PREFIX + url;
        processedUrlStore.save(key, url, Duration.ofSeconds(ttl));
    }

    public boolean existsByUrl(String url) {
        String key = PROCESSED_URL_PREFIX + url;
        return processedUrlStore.exists(key);
    }
}
