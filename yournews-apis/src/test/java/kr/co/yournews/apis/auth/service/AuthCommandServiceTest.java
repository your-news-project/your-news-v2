package kr.co.yournews.apis.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.yournews.auth.dto.SignInDto;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.auth.helper.JwtHelper;
import kr.co.yournews.auth.service.PasswordEncodeService;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthCommandServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncodeService passwordEncodeService;

    @Mock
    private JwtHelper jwtHelper;

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

    @Test
    @DisplayName("회원가입 시 토큰 반환")
    void signUpTest() {
        // given
        SignUpDto.Auth signUpDto = new SignUpDto.Auth(username, password, nickname, "test@gmail.com");

        given(passwordEncodeService.encode(password)).willReturn(encodedPassword);
        given(jwtHelper.createToken(any(User.class))).willReturn(tokenDto);

        // when
        TokenDto result = authCommandService.signUp(signUpDto);

        // then
        verify(userService).save(any(User.class));
        verify(jwtHelper).createToken(any(User.class));
        assertEquals(tokenDto.accessToken(), result.accessToken());
        assertEquals(tokenDto.refreshToken(), result.refreshToken());
    }

    @Test
    @DisplayName("로그인 성공 시 토큰 반환")
    void signInTest() {
        // given
        SignInDto signInDto = new SignInDto(username, password);

        given(userService.readByUsername(username)).willReturn(Optional.of(user));
        given(passwordEncodeService.matches(password, encodedPassword)).willReturn(true);
        given(jwtHelper.createToken(user)).willReturn(tokenDto);

        // when
        TokenDto result = authCommandService.signIn(signInDto);

        // then
        assertEquals(tokenDto.accessToken(), result.accessToken());
        verify(jwtHelper).createToken(user);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void signInFail_UserNotFound() {
        // given
        SignInDto signInDto = new SignInDto(username, password);
        given(userService.readByUsername(username)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> authCommandService.signIn(signInDto));

        // then
        assertEquals(UserErrorType.NOT_FOUND, exception.getErrorType());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void signInFail_InvalidPassword() {
        // given
        SignInDto signInDto = new SignInDto(username, password);
        given(userService.readByUsername(username)).willReturn(Optional.of(user));
        given(passwordEncodeService.matches(password, encodedPassword)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> authCommandService.signIn(signInDto));

        // then
        assertEquals(UserErrorType.NOT_MATCHED_PASSWORD, exception.getErrorType());
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
        verify(jwtHelper).reissueToken(refreshToken);
    }

    @Test
    @DisplayName("로그아웃 시 토큰 삭제")
    void signOutTest() {
        // given
        String refreshToken = "refreshToken";
        HttpServletResponse response = new MockHttpServletResponse();

        // when
        authCommandService.signOut(refreshToken, response);

        // then
        verify(jwtHelper).removeToken(refreshToken, response);
    }
}
