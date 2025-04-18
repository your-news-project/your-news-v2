package kr.co.yournews.apis.auth.service.mail;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.infra.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static kr.co.yournews.infra.redis.util.RedisConstants.CODE_PREFIX;
import static kr.co.yournews.infra.redis.util.RedisConstants.VERIFIED_PREFIX;

@Service
@RequiredArgsConstructor
public class AuthCodeService {
    private final RedisRepository redisRepository;
    private final UserService userService;

    private static final Duration TTL = Duration.ofMinutes(3);
    private static final long RESEND_THRESHOLD_SECONDS = 2 * 60;

    /**
     * 이메일로 인증 코드를 생성하고 Redis에 저장
     *
     * @param email : 인증 코드를 받을 이메일
     * @return : 생성된 인증 코드 문자열
     * @throws CustomException EXIST_EMAIL: 이미 존재하는 이메일
     *                         ALREADY_MAIL_REQUEST: 빠른 재요청
     */
    public String generateAndSave(String email) {
        if (userService.existsByEmail(email)) {
            throw new CustomException(UserErrorType.EXIST_EMAIL);
        }

        if (!canResend(email)) {
            throw new CustomException(UserErrorType.ALREADY_MAIL_REQUEST);
        }

        String key = CODE_PREFIX + email;
        String code = generateCode();
        redisRepository.set(key, code, TTL);

        return code;
    }

    /**
     * 인증 코드 재요청이 가능한지 확인
     * 현재 남은 만료 시간이 일정 기준 이하일 경우에만 재요청을 허용
     *
     * @param email : 사용자 이메일
     * @return : 재요청 가능 여부 (true: 가능, false: 불가능)
     */
    private boolean canResend(String email) {
        Long expireSec = redisRepository.getExpire(
                CODE_PREFIX + email,
                TimeUnit.SECONDS
        );
        return expireSec == null || expireSec <= RESEND_THRESHOLD_SECONDS;
    }

    /**
     * 6자리 랜덤 숫자 형태의 인증 코드를 생성
     *
     * @return : 인증 코드 (문자열 형태의 6자리 숫자)
     */
    private String generateCode() {
        int code = (int) (Math.random() * 900_000) + 100_000;
        return String.valueOf(code);
    }

    /**
     * 사용자가 입력한 인증 코드가 Redis에 저장된 값과 일치하는지 검증
     * - 인증 성공하면, 인증된 이메일 Redis 저장 (최종 회원가입 때, 확인을 위해)
     *
     * @param email : 사용자 이메일
     * @param code  : 입력한 인증 코드
     * @return : 인증 성공 여부 (일치하면 true)
     * @throws CustomException CODE_EXPIRED: 저장된 코드 없음
     *                         INVALID_CODE: 코드 불일치
     */
    public boolean verifiedCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String storedCode = (String) redisRepository.get(key);

        if (storedCode == null) {
            throw new CustomException(UserErrorType.CODE_EXPIRED);
        }

        if (!storedCode.equals(code)) {
            throw new CustomException(UserErrorType.INVALID_CODE);
        }

        redisRepository.set(VERIFIED_PREFIX + email, true, Duration.ofMinutes(10));
        redisRepository.del(key);

        return true;
    }

    /**
     * 이메일 인증 여부를 검증하고 인증 상태를 삭제
     * - 인증 여부는 Redis에 저장된 VERIFIED_PREFIX + email 값을 통해 확인
     * - 인증 여부가 확인되지 않거나 false인 경우 예외를 발생
     * - 인증 여부가 true이면 Redis에서 해당 키를 삭제하여 재사용을 방지
     *
     * @param email : 인증 상태를 확인할 사용자 이메일
     * @throws CustomException CODE_NOT_VERIFIED: 인증되지 않은 경우
     */
    public void ensureVerifiedAndConsume(String email) {
        String key = VERIFIED_PREFIX + email;
        Boolean isVerified = (Boolean) redisRepository.get(key);

        if (isVerified == null || !isVerified) {
            throw new CustomException(UserErrorType.CODE_NOT_VERIFIED);
        }

        redisRepository.del(key);
    }
}
