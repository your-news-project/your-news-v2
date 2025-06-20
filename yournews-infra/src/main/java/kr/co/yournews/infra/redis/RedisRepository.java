package kr.co.yournews.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    /* Redis 저장 */
    public void set(String key, Object value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    /* Redis List 저장 */
    public <T> void setListAll(String key, Collection<T> values) {
        redisTemplate.opsForList().rightPushAll(key, new ArrayList<>(values));
    }

    /* Redis 정보 가져오기 */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /* Redis List 조회 */
    public <T> List<T> getList(String key, Class<T> clazz) {
        List<Object> objects = redisTemplate.opsForList().range(key, 0, -1);
        if (objects == null || objects.isEmpty()) {
            return Collections.emptyList();
        }

        return objects.stream()
                .map(clazz::cast)
                .collect(Collectors.toList());
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
