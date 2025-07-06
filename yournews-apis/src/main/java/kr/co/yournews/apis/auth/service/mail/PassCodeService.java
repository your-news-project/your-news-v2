package kr.co.yournews.apis.auth.service.mail;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.infra.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

import static kr.co.yournews.infra.redis.util.RedisConstants.PASS_KEY_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassCodeService {
    private final RedisRepository redisRepository;

    private static final Duration TTL = Duration.ofMinutes(10);

    /**
     * 비밀번호 재설정 링크용 UUID를 생성하고 Redis에 저장
     *
     * @param username : 사용자 아이디
     * @return : 생성된 UUID 문자열
     */
    public String generateResetUuidAndStore(String username) {
        String randomUUID = generateUuid();
        String key = PASS_KEY_PREFIX + username;
        redisRepository.set(key, randomUUID, TTL);

        log.info("[비밀전호 재설정 UUID 생성 및 저장 완료] username: {}, uuid: {}", username, randomUUID);
        return randomUUID;
    }

    private String generateUuid() {
        return String.valueOf(UUID.randomUUID());
    }

    /**
     * 전달한 UUID가 Redis에 저장된 값과 일치하는지 검증
     *
     * @param username : 사용자 아이디
     * @param uuid     : 전달한 uuid
     */
    public void validateResetUuid(String username, String uuid) {
        String key = PASS_KEY_PREFIX + username;
        String savedCode = (String) redisRepository.get(key);

        if (savedCode == null) {
            throw new CustomException(UserErrorType.EXPIRED_PASSWORD_RESET_CODE);
        }

        if (!uuid.equals(savedCode)) {
            throw new CustomException(UserErrorType.INVALID_PASSWORD_RESET_REQUEST);
        }

        redisRepository.del(key);

        log.info("[UUID 검증 성공] username: {}", username);
    }
}
