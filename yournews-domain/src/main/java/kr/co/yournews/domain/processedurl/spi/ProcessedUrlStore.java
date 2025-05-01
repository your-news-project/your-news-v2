package kr.co.yournews.domain.processedurl.spi;


import java.time.Duration;

public interface ProcessedUrlStore {
    void save(String key, String url, Duration ttl);
    boolean exists(String url);
}
