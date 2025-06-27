package kr.co.yournews.apis.auth.service.mail;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.infra.redis.RedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static kr.co.yournews.infra.redis.util.RedisConstants.PASS_KEY_PREFIX;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PassCodeServiceTest {

    @Mock
    private RedisRepository redisRepository;

    @InjectMocks
    private PassCodeService passCodeService;

    @Test
    @DisplayName("UUID 생성 및 저장 테스트")
    void generateResetUuidAndStoreTest() {
        // given
        String username = "testUser";

        // when
        String result = passCodeService.generateResetUuidAndStore(username);

        // then
        assertNotNull(result);
        verify(redisRepository, times(1))
                .set(startsWith(PASS_KEY_PREFIX), eq(result), eq(Duration.ofMinutes(10)));
    }

    @Test
    @DisplayName("UUID 검증 성공 테스트")
    void validateResetUuidSuccessTest() {
        // given
        String username = "testUser";
        String uuid = "uuid";
        given(redisRepository.get(PASS_KEY_PREFIX + username)).willReturn(uuid);

        // when
        assertDoesNotThrow(() -> passCodeService.validateResetUuid(username, uuid));

        // then
        verify(redisRepository, times(1)).del(PASS_KEY_PREFIX + username);
    }

    @Test
    @DisplayName("UUID 검증 실패 - 일치하지 않음")
    void validateResetUuidNotMatchTest() {
        // given
        String username = "testUser";
        String saved = "uuid-1234";
        String input = "wrong-uuid";
        given(redisRepository.get(PASS_KEY_PREFIX + username)).willReturn(saved);

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> passCodeService.validateResetUuid(username, input));

        // then
        assertEquals(UserErrorType.INVALID_PASSWORD_RESET_REQUEST, ex.getErrorType());

        verify(redisRepository, never()).del(anyString());
    }

    @Test
    @DisplayName("UUID 검증 실패 - Redis에 없음")
    void validateResetUuidExpiredTest() {
        // given
        String username = "testUser";
        given(redisRepository.get(PASS_KEY_PREFIX + username)).willReturn(null);

        // when
        CustomException ex = assertThrows(CustomException.class,
                () -> passCodeService.validateResetUuid(username, "invalid"));

        // then
        assertEquals(UserErrorType.EXPIRED_PASSWORD_RESET_CODE, ex.getErrorType());
        verify(redisRepository, never()).del(anyString());
    }
}
