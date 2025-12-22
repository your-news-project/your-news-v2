package kr.co.yournews.apis.auth.service;

import kr.co.yournews.apis.auth.dto.RestoreUserDto;
import kr.co.yournews.apis.auth.dto.SignOutDto;
import kr.co.yournews.apis.auth.service.mail.AuthCodeService;
import kr.co.yournews.apis.news.service.SubNewsCommandService;
import kr.co.yournews.apis.user.service.FcmTokenCommandService;
import kr.co.yournews.auth.dto.SignInDto;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.auth.helper.JwtHelper;
import kr.co.yournews.auth.helper.TokenMode;
import kr.co.yournews.auth.service.PasswordEncodeService;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthCommandServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncodeService passwordEncodeService;

    @Mock
    private JwtHelper jwtHelper;

    @Mock
    private AuthCodeService authCodeService;

    @Mock
    private SubNewsCommandService subNewsCommandService;

    @Mock
    private FcmTokenCommandService fcmTokenCommandService;

    @InjectMocks
    private AuthCommandService authCommandService;

    private final String username = "testuser";
    private final String password = "1234";
    private final String encodedPassword = "encoded-1234";
    private final String nickname = "테스터";
    private final Long userId = 1L;

    private User user;
    private TokenDto tokenDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username(username)
                .nickname(nickname)
                .password(encodedPassword)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        tokenDto = TokenDto.of("accessToken", "refreshToken");
    }

    @Nested
    @DisplayName("회원가입")
    class SignUpTest {
        private final String email = "test@gmail.com";

        @Test
        @DisplayName("회원가입 성공 시 토큰 반환")
        void signUpSuccess() {
            // given
            SignUpDto.Auth signUpDto =
                    new SignUpDto.Auth(
                            username, password, nickname, email,
                            List.of(1L, 2L, 3L), List.of("키워드1", "키워드2"),
                            true, true, true
                    );

            given(passwordEncodeService.encode(password)).willReturn(encodedPassword);
            given(jwtHelper.createToken(any(User.class), any(TokenMode.class))).willReturn(tokenDto);

            // when
            TokenDto result = authCommandService.signUp(signUpDto);

            // then
            verify(authCodeService).ensureVerifiedAndConsume(email);
            verify(userService).save(any(User.class));
            verify(jwtHelper).createToken(any(User.class), any(TokenMode.class));

            assertEquals(tokenDto.accessToken(), result.accessToken());
            assertEquals(tokenDto.refreshToken(), result.refreshToken());
        }

        @Test
        @DisplayName("회원가입 실패 - 인증되지 않은 이메일")
        void signUpFailIfCodeNotVerified() {
            // given
            SignUpDto.Auth signUpDto =
                    new SignUpDto.Auth(
                            username, password, nickname, email,
                            List.of(1L, 2L, 3L), List.of("키워드1", "키워드2"),
                            true, true, true
                    );

            doThrow(new CustomException(UserErrorType.CODE_NOT_VERIFIED))
                    .when(authCodeService).ensureVerifiedAndConsume(email);

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> authCommandService.signUp(signUpDto));

            // then
            assertEquals(UserErrorType.CODE_NOT_VERIFIED, exception.getErrorType());
            verify(authCodeService).ensureVerifiedAndConsume(email);
            verify(userService, never()).save(any());
            verify(jwtHelper, never()).createToken(any(), any());
        }
    }

    @Nested
    @DisplayName("로그인")
    class SignInTest {

        private final SignInDto signInDto = new SignInDto(username, password);

        @Test
        @DisplayName("로그인 성공 시 토큰 반환")
        void signInSuccess() {
            // given
            given(userService.readByUsernameIncludeDeleted(username)).willReturn(Optional.of(user));
            given(passwordEncodeService.matches(password, encodedPassword)).willReturn(true);
            given(jwtHelper.createToken(user, TokenMode.FULL)).willReturn(tokenDto);

            // when
            TokenDto result = authCommandService.signIn(signInDto);

            // then
            assertEquals(tokenDto.accessToken(), result.accessToken());
            verify(jwtHelper, times(1)).createToken(user, TokenMode.FULL);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void signInFailUserNotFound() {
            // given
            given(userService.readByUsernameIncludeDeleted(username)).willReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> authCommandService.signIn(signInDto));

            // then
            assertEquals(UserErrorType.NOT_FOUND, exception.getErrorType());
            verify(jwtHelper, never()).createToken(user, TokenMode.FULL);
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void signInFailInvalidPassword() {
            // given
            given(userService.readByUsernameIncludeDeleted(username)).willReturn(Optional.of(user));
            given(passwordEncodeService.matches(password, encodedPassword)).willReturn(false);

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> authCommandService.signIn(signInDto));

            // then
            assertEquals(UserErrorType.NOT_MATCHED_PASSWORD, exception.getErrorType());
            verify(jwtHelper, never()).createToken(user, TokenMode.FULL);
        }

        @Test
        @DisplayName("실패 - Soft Deleted User")
        void signInFailBySoftDeletedUser() {
            // given
            user = User.builder()
                    .username(username)
                    .nickname(nickname)
                    .password(encodedPassword)
                    .build();
            ReflectionTestUtils.setField(user, "id", userId);
            ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now().minusDays(1));

            given(userService.readByUsernameIncludeDeleted(username)).willReturn(Optional.of(user));
            given(passwordEncodeService.matches(password, user.getPassword())).willReturn(true);

            // when
            CustomException exception = assertThrows(CustomException.class, () ->
                authCommandService.signIn(signInDto)
            );

            // then
            assertEquals(UserErrorType.DEACTIVATED, exception.getErrorType());
            verify(jwtHelper, never()).createToken(user, TokenMode.FULL);
        }
    }

    @Nested
    @DisplayName("계정 복구 테스트")
    class RestoreUserTest {

        private final RestoreUserDto.Request request = new RestoreUserDto.Request(username);

        @Test
        @DisplayName("성공")
        void restoreDeletedUserSuccess() {
            // given
            user = User.builder()
                    .username(username)
                    .nickname(nickname)
                    .password(encodedPassword)
                    .build();
            ReflectionTestUtils.setField(user, "id", userId);
            ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now().minusDays(2));

            given(userService.readByUsernameIncludeDeleted(username)).willReturn(Optional.of(user));
            given(jwtHelper.createToken(user, TokenMode.FULL)).willReturn(tokenDto);

            // when
            TokenDto result = authCommandService.restoreUser(request);

            // then
            assertEquals(tokenDto.accessToken(), result.accessToken());
            assertEquals(tokenDto.refreshToken(), result.refreshToken());
            assertNull(user.getDeletedAt());
            verify(jwtHelper, times(1)).createToken(user, TokenMode.FULL);
        }

        @Test
        @DisplayName("실패 - 이미 활성화된 사용자")
        void restoreAlreadyActiveUserFail() {
            // given
            given(userService.readByUsernameIncludeDeleted(username))
                    .willReturn(Optional.of(user)); // 전체 클래스에 지정된 이미 존재하는 사용자

            // when
            CustomException exception = assertThrows(CustomException.class, () ->
                authCommandService.restoreUser(request)
            );

            // then
            assertEquals(UserErrorType.ALREADY_ACTIVE, exception.getErrorType());
            verify(jwtHelper, never()).createToken(user, TokenMode.FULL);
        }
    }

    @Test
    @DisplayName("access token 재발급")
    void reissueAccessTokenTest() {
        // given
        String refreshToken = "refreshToken";
        given(jwtHelper.reissueToken(refreshToken)).willReturn(tokenDto);

        // when
        TokenDto result = authCommandService.reissueAccessToken(refreshToken);

        // then
        assertEquals(tokenDto.refreshToken(), result.refreshToken());
        assertEquals(tokenDto.accessToken(), result.accessToken());
        verify(jwtHelper, times(1)).reissueToken(refreshToken);
    }

    @Test
    @DisplayName("로그아웃 시 토큰 삭제")
    void signOutTest() {
        // given
        String accessTokenInHeader = "Bearer accessToken";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        SignOutDto signOutDto = new SignOutDto("device-info");

        // when
        authCommandService.signOut(accessTokenInHeader, refreshToken, userId, signOutDto);

        // then
        verify(jwtHelper, times(1)).removeToken(accessToken, refreshToken);
    }
}
