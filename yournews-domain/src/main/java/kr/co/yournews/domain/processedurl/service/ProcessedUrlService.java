package kr.co.yournews.domain.processedurl.service;

import kr.co.yournews.infra.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static kr.co.yournews.infra.redis.util.RedisConstants.PROCESSED_URL_PREFIX;

@Service
@RequiredArgsConstructor
public class ProcessedUrlService {
    private final RedisRepository redisRepository;

    public void save(String url, long ttl) {
        String key = PROCESSED_URL_PREFIX + url;
        redisRepository.set(key, url, Duration.ofSeconds(ttl));
    }

    public boolean existsByUrl(String url) {
        return redisRepository.existed(PROCESSED_URL_PREFIX + url);
    }
}
