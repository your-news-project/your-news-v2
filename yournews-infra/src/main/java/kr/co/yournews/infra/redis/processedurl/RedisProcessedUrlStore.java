package kr.co.yournews.infra.redis.processedurl;

import kr.co.yournews.domain.processedurl.spi.ProcessedUrlStore;
import kr.co.yournews.infra.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisProcessedUrlStore implements ProcessedUrlStore {
    private final RedisRepository redisRepository;

    @Override
    public void save(String key, String url, Duration ttl) {
        redisRepository.set(key, url, ttl);
    }

    @Override
    public boolean exists(String key) {
        return redisRepository.existed(key);
    }
}
