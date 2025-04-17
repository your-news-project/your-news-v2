package kr.co.yournews.apis.auth.service.mail;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.infra.redis.RedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static kr.co.yournews.infra.redis.util.RedisConstants.CODE_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthCodeServiceTest {

    @Mock
    private RedisRepository redisRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthCodeService authCodeService;

    private final String email = "test@example.com";

    @Nested
    @DisplayName("인증 코드 생성")
    class GenerateCodeTest {

        @Test
        @DisplayName("성공")
        void generateAndSaveSuccess() {
            // given
            given(userService.existsByEmail(email)).willReturn(false);
            given(redisRepository.getExpire(anyString(), eq(TimeUnit.SECONDS))).willReturn(null);

            // when
            String code = authCodeService.generateAndSave(email);

            // then
            assertNotNull(code);
            assertEquals(6, code.length());
            verify(redisRepository).set(startsWith(CODE_PREFIX), eq(code), eq(Duration.ofMinutes(3)));
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 이메일")
        void generateAndSaveFailExistEmail() {
            // given
            given(userService.existsByEmail(email)).willReturn(true);

            // when
            CustomException e = assertThrows(CustomException.class,
                    () -> authCodeService.generateAndSave(email));

            // then
            assertEquals(UserErrorType.EXIST_EMAIL, e.getErrorType());
        }

        @Test
        @DisplayName("실패 - 너무 빠른 재요청")
        void generateAndSaveFailResendTooFast() {
            // given
            given(userService.existsByEmail(email)).willReturn(false);
            given(redisRepository.getExpire(anyString(), eq(TimeUnit.SECONDS))).willReturn(150L);

            // when
            CustomException e = assertThrows(CustomException.class,
                    () -> authCodeService.generateAndSave(email));

            // then
            assertEquals(UserErrorType.ALREADY_MAIL_REQUEST, e.getErrorType());
        }
    }

    @Nested
    @DisplayName("인증 코드 검증")
    class VerifyCodeTest {

        @Test
        @DisplayName("성공")
        void verifyCodeSuccess() {
            // given
            String code = "123456";
            given(redisRepository.get(CODE_PREFIX + email)).willReturn(code);

            // when
            boolean result = authCodeService.verifiedCode(email, code);

            // then
            assertTrue(result);
        }

        @Test
        @DisplayName("실패 - 저장된 코드 없음")
        void verifyCodeFailCodeExpired() {
            // given
            given(redisRepository.get(CODE_PREFIX + email)).willReturn(null);

            // when
            CustomException e = assertThrows(CustomException.class,
                    () -> authCodeService.verifiedCode(email, "any"));

            // then
            assertEquals(UserErrorType.CODE_EXPIRED, e.getErrorType());
        }

        @Test
        @DisplayName("실패 - 코드 불일치")
        void verifyCodeFailInvalidCode() {
            // given
            given(redisRepository.get(CODE_PREFIX + email)).willReturn("654321");

            // when
            CustomException e = assertThrows(CustomException.class,
                    () -> authCodeService.verifiedCode(email, "123456"));

            // then
            assertEquals(UserErrorType.INVALID_CODE, e.getErrorType());
        }
    }
}
