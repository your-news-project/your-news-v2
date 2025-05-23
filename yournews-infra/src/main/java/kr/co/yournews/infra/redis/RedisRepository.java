package kr.co.yournews.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    /* Redis 저장 */
    public void set(String key, Object value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    /* Redis 정보 가져오기 */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /* Redis 삭제 */
    public void del(String key) {
        redisTemplate.delete(key);
    }

    /* Redis 존재 확인 */
    public boolean existed(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /* 남은 expired 시간 가져오기 */
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

}
