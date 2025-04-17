package kr.co.yournews.auth.service;

import kr.co.yournews.infra.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import static kr.co.yournews.infra.redis.util.RedisConstants.BLACKLIST_KEY_PREFIX;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {
    private final RedisRepository redisRepository;

    /**
     * AccessToken을 Redis에 블랙리스트로 등록
     * - 로그아웃된 토큰 등 더 이상 유효하지 않은 토큰을 차단하기 위한 용도
     *
     * @param accessToken : 블랙리스트에 등록할 JWT access token
     * @param expireAt    : 해당 토큰의 만료 시간 (access token의 exp 기준)
     */
    public void saveBlackList(String accessToken, LocalDateTime expireAt) {
        String key = BLACKLIST_KEY_PREFIX + accessToken;
        LocalDateTime now = LocalDateTime.now();
        long timeToLive = Duration.between(now, expireAt).toSeconds();

        redisRepository.set(key, accessToken, Duration.ofSeconds(timeToLive));
    }

    /**
     * 주어진 accessToken이 블랙리스트에 등록되어 있는지 확인
     * - 등록되어 있다면 해당 토큰은 사용할 수 없음
     *
     * @param accessToken : 검증할 access token
     * @return true: 블랙리스트에 있음 (사용 불가), false: 없음 (사용 가능)
     */
    public boolean existsBlackListCheck(String accessToken) {
        return redisRepository.existed(BLACKLIST_KEY_PREFIX + accessToken);
    }

}
