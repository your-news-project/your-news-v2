package kr.co.yournews.apis.user.service;

import kr.co.yournews.apis.user.dto.UserReq;
import kr.co.yournews.auth.service.PasswordEncodeService;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserCommandServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncodeService passwordEncodeService;

    @Mock
    private FcmTokenService fcmTokenService;

    @InjectMocks
    private UserCommandService userCommandService;

    private final Long userId = 1L;

    @Nested
    @DisplayName("비밀번호 변경")
    class UpdatePasswordTest {

        @Test
        @DisplayName("성공")
        void updatePasswordSuccess() {
            // given
            String currentPassword = "oldPass123!";
            String newPassword = "newPass456!";
            String encodedNewPassword = "encodedNewPass";

            User user = User.builder()
                    .username("test")
                    .password(currentPassword)
                    .nickname("테스터")
                    .build();

            UserReq.UpdatePassword dto = new UserReq.UpdatePassword(currentPassword, newPassword);

            given(userService.readById(userId)).willReturn(Optional.of(user));
            given(passwordEncodeService.matches(currentPassword, user.getPassword())).willReturn(true);
            given(passwordEncodeService.encode(newPassword)).willReturn(encodedNewPassword);

            // when
            userCommandService.updatePassword(userId, dto);

            // then
            assertEquals(encodedNewPassword, user.getPassword());
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void updatePasswordFailDueToMismatch() {
            // given
            UserReq.UpdatePassword dto = new UserReq.UpdatePassword("wrongCurrentPass", "newPass");
            User mockUser = mock(User.class);
            given(userService.readById(userId)).willReturn(Optional.of(mockUser));
            given(passwordEncodeService.matches("wrongCurrentPass", mockUser.getPassword())).willReturn(false);

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> userCommandService.updatePassword(userId, dto));

            // then
            assertEquals(UserErrorType.NOT_MATCHED_PASSWORD, exception.getErrorType());
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void updatePasswordFailUserNotFound() {
            // given
            UserReq.UpdatePassword dto = new UserReq.UpdatePassword("old", "new");
            given(userService.readById(userId)).willReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> userCommandService.updatePassword(userId, dto));

            // then
            assertEquals(UserErrorType.NOT_FOUND, exception.getErrorType());
        }
    }

    @Nested
    @DisplayName("회원 프로필 수정")
    class UpdateUserProfileTest {

        @Test
        @DisplayName("성공")
        void updateNicknameSuccess() {
            // given
            User user = User.builder()
                    .username("testuser")
                    .nickname("oldNickname")
                    .build();

            UserReq.UpdateProfile dto = new UserReq.UpdateProfile("newNickname");

            given(userService.readById(userId)).willReturn(Optional.of(user));
            given(userService.existsByNickname("newNickname")).willReturn(false);

            // when
            userCommandService.updateUserProfile(userId, dto);

            // then
            assertEquals("newNickname", user.getNickname());
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void updateNicknameFail_UserNotFound() {
            // given
            UserReq.UpdateProfile dto = new UserReq.UpdateProfile("newNickname");
            given(userService.readById(userId)).willReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> userCommandService.updateUserProfile(userId, dto));

            // then
            assertEquals(UserErrorType.NOT_FOUND, exception.getErrorType());
        }

        @Test
        @DisplayName("실패 - 닉네임 중복")
        void updateNicknameFail_DuplicateNickname() {
            // given
            User user = User.builder()
                    .username("testuser")
                    .nickname("oldNickname")
                    .build();

            UserReq.UpdateProfile dto = new UserReq.UpdateProfile("newNickname");

            given(userService.readById(userId)).willReturn(Optional.of(user));
            given(userService.existsByNickname("newNickname")).willReturn(true);

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> userCommandService.updateUserProfile(userId, dto));

            // then
            assertEquals(UserErrorType.EXIST_NICKNAME, exception.getErrorType());
        }
    }

    @Nested
    @DisplayName("사용자 구독 상태 변경")
    class UpdateSubStatusTest {

        @Test
        @DisplayName("성공")
        void updateSubStatusSuccess() {
            // given
            User user = User.builder()
                    .username("testuser")
                    .nickname("nick")
                    .build();

            UserReq.UpdateStatus dto = new UserReq.UpdateStatus(true, false);

            given(userService.readById(userId)).willReturn(Optional.of(user));

            // when
            userCommandService.updateSubStatus(userId, dto);

            // then
            assertTrue(user.isSubStatus());
            assertFalse(user.isDailySubStatus());
        }

        @Test
        @DisplayName("실패 - 사용자 없음")
        void updateSubStatusFail_UserNotFound() {
            // given
            UserReq.UpdateStatus dto = new UserReq.UpdateStatus(true, false);
            given(userService.readById(userId)).willReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> userCommandService.updateSubStatus(userId, dto));

            // then
            assertEquals(UserErrorType.NOT_FOUND, exception.getErrorType());
        }
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteUserTest() {
        // given

        // when
        userCommandService.deleteUser(userId);

        // then
        verify(userService, times(1)).deleteById(userId);
        verify(fcmTokenService, times(1)).deleteAllByUserId(userId);
    }
}
