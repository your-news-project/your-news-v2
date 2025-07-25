package kr.co.yournews.infra.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
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

    /* ZSET: 소식 랭킹 점수 증가 */
    public void incrementZSetScore(String key, String member, int score, Duration duration) {
        redisTemplate.opsForZSet().incrementScore(key, member, score);
        redisTemplate.expire(key, duration);
    }

    /* ZSET: 상위 랭킹 뉴스 조회 */
    public <T> List<T> getTopZSetWithScore(String key, int topN, BiFunction<String, Integer, T> mapper) {
        Set<ZSetOperations.TypedTuple<Object>> results =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, topN - 1);
        if (results == null) return Collections.emptyList();

        return results.stream()
                .map(tuple -> {
                    String name = String.valueOf(tuple.getValue());
                    int score = (tuple.getScore() != null) ? tuple.getScore().intValue() : 0;
                    return mapper.apply(name, score);
                })
                .collect(Collectors.toList());
    }
}
