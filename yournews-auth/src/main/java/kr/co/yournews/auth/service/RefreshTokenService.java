package kr.co.yournews.auth.service;

import kr.co.yournews.infra.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static kr.co.yournews.infra.redis.util.RedisConstants.REFRESH_TOKEN_PREFIX;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisRepository redisRepository;

    @Value("${token.refresh.in-redis}")
    private long REDIS_REFRESH_EXPIRATION;

    /**
     * redis에 refreshToken 저장
     *
     * @param username     : 사용자 아이디
     * @param refreshToken : 사용자의 refreshToken
     */
    public void saveRefreshToken(String username, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + username;
        redisRepository.set(
                key,
                refreshToken,
                Duration.ofSeconds(REDIS_REFRESH_EXPIRATION)
        );
    }

    /**
     * redis에 refreshToken 존재 확인
     *
     * @param username : 사용자 아이디
     * @return : 존재여부
     */
    public boolean existedRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;

        return redisRepository.existed(key);
    }

    /**
     * redis에서 refreshToken 삭제
     *
     * @param username : 사용자 아이디
     */
    public void deleteRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        redisRepository.del(key);
    }
}
