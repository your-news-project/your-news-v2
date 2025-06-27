package kr.co.yournews.apis.auth.service.mail;

import kr.co.yournews.apis.auth.dto.PassResetDto;
import kr.co.yournews.auth.service.PasswordEncodeService;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.infra.mail.MailSenderAdapter;
import kr.co.yournews.infra.mail.strategy.MailStrategyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PassCodeManagerTest {

    @Mock
    private UserService userService;

    @Mock
    private PassCodeService passCodeService;

    @Mock
    private MailSenderAdapter mailSenderAdapter;

    @Mock
    private MailStrategyFactory mailStrategyFactory;

    @Mock
    private PasswordEncodeService passwordEncodeService;

    @InjectMocks
    private PassCodeManager passCodeManager;

    @Nested
    @DisplayName("비밀번호 재설정 요청 initiatePasswordReset")
    class InitiatePasswordResetTest {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            PassResetDto.VerifyUser dto = new PassResetDto.VerifyUser("user1", "user1@email.com");

            given(userService.existsByUsernameAndEmail(dto.username(), dto.email())).willReturn(true);
            given(passCodeService.generateResetUuidAndStore(dto.username())).willReturn("uuid-1234");

            // when
            assertDoesNotThrow(() -> passCodeManager.initiatePasswordReset(dto));

            // then
            verify(mailSenderAdapter).sendMail(eq(dto.email()), eq("uuid-1234"), any());
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void failNotExistUser() {
            // given
            PassResetDto.VerifyUser dto = new PassResetDto.VerifyUser("userX", "userX@email.com");
            given(userService.existsByUsernameAndEmail(dto.username(), dto.email())).willReturn(false);

            // when
            CustomException ex = assertThrows(CustomException.class,
                    () -> passCodeManager.initiatePasswordReset(dto));

            // then
            assertEquals(UserErrorType.INVALID_USER_INFO, ex.getErrorType());
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 applyNewPassword")
    class ApplyNewPasswordTest {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            PassResetDto.ResetPassword dto =
                    new PassResetDto.ResetPassword("user1", "uuid-1234", "newPassword");

            User mockUser = mock(User.class);

            given(userService.readByUsername(dto.username())).willReturn(Optional.of(mockUser));
            given(passwordEncodeService.encode(dto.password())).willReturn("encoded");

            // when
            assertDoesNotThrow(() -> passCodeManager.applyNewPassword(dto));

            // then
            verify(mockUser).updatePassword("encoded");
        }

        @Test
        @DisplayName("실패 - UUID 불일치")
        void failInvalidUuid() {
            // given
            PassResetDto.ResetPassword dto =
                    new PassResetDto.ResetPassword("user1", "invalid-uuid", "newPassword");

            doThrow(new CustomException(UserErrorType.INVALID_PASSWORD_RESET_REQUEST))
                    .when(passCodeService)
                    .validateResetUuid(dto.username(), dto.uuid());

            // when
            CustomException ex = assertThrows(CustomException.class,
                    () -> passCodeManager.applyNewPassword(dto));

            // then
            assertEquals(UserErrorType.INVALID_PASSWORD_RESET_REQUEST, ex.getErrorType());
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void failUserNotFound() {
            // given
            PassResetDto.ResetPassword dto =
                    new PassResetDto.ResetPassword("user1", "uuid-1234", "newPassword");

            given(userService.readByUsername(dto.username())).willReturn(Optional.empty());

            // when
            CustomException ex = assertThrows(CustomException.class,
                    () -> passCodeManager.applyNewPassword(dto));

            // then
            assertEquals(UserErrorType.NOT_FOUND, ex.getErrorType());
        }
    }
}
