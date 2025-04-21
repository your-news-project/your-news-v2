package kr.co.yournews.apis.auth.service;

import kr.co.yournews.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserValidationServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserValidationService userValidationService;

    @Nested
    @DisplayName("아이디 중복 확인")
    class CheckUsernameDuplicationTest {

        @Test
        @DisplayName("존재 - true 반환")
        void isUsernameExistsTrue() {
            // given
            String username = "testuser";
            given(userService.existsByUsername(username)).willReturn(true);

            // when
            boolean exists = userValidationService.isUsernameExists(username);

            // then
            assertTrue(exists);
        }

        @Test
        @DisplayName("존재하지 않음 - false 반환")
        void isUsernameExistsFalse() {
            // given
            String username = "newuser";
            given(userService.existsByUsername(username)).willReturn(false);

            // when
            boolean exists = userValidationService.isUsernameExists(username);

            // then
            assertFalse(exists);
        }
    }

    @Nested
    @DisplayName("닉네임 중복 확인")
    class CheckNicknameDuplicationTest {

        @Test
        @DisplayName("존재 - true 반환")
        void isNicknameExistsTrue() {
            // given
            String nickname = "닉네임";
            given(userService.existsByNickname(nickname)).willReturn(true);

            // when
            boolean exists = userValidationService.isNicknameExists(nickname);

            // then
            assertTrue(exists);
        }

        @Test
        @DisplayName("존재하지 않음 - false 반환")
        void isNicknameExistsFalse() {
            // given
            String nickname = "새닉네임";
            given(userService.existsByNickname(nickname)).willReturn(false);

            // when
            boolean exists = userValidationService.isNicknameExists(nickname);

            // then
            assertFalse(exists);
        }
    }
}
