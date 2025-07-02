package kr.co.yournews.apis.auth.service;

import kr.co.yournews.apis.auth.dto.OAuthCode;
import kr.co.yournews.apis.auth.dto.OAuthTokenDto;
import kr.co.yournews.apis.news.service.SubNewsCommandService;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.auth.helper.JwtHelper;
import kr.co.yournews.auth.helper.TokenMode;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.domain.user.type.OAuthPlatform;
import kr.co.yournews.infra.oauth.OAuthClient;
import kr.co.yournews.infra.oauth.dto.OAuthUserInfoRes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OAuthCommandServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private OAuthClientFactory oAuthClientFactory;

    @Mock
    private JwtHelper jwtHelper;

    @Mock
    private OAuthClient oAuthClient;

    @Mock
    private SubNewsCommandService subNewsCommandService;

    @InjectMocks
    private OAuthCommandService oAuthCommandService;

    private final OAuthPlatform platform = OAuthPlatform.NAVER;
    private final OAuthCode oAuthCode = new OAuthCode("authCode123");

    private final OAuthUserInfoRes userInfoRes = new OAuthUserInfoRes("oauthId123", "test@naver.com");

    private final String username = platform.name().toLowerCase() + "_" + userInfoRes.id();

    private final User user = User.builder()
            .username("nickname_oauthId123")
            .nickname("nickname_oauthId123")
            .email("test@naver.com")
            .platform(platform)
            .signedUp(true)
            .build();

    private final TokenDto tokenDto = TokenDto.of("access", "refresh");

    @Test
    @DisplayName("회원가입 시 토큰 반환")
    void signUpTest() {
        // given
        SignUpDto.OAuth signUpDto =
                new SignUpDto.OAuth("test", List.of(1L, 2L, 3L), List.of("키워드1", "키워드2"), true, true);
        Long userId = 1L;

        given(userService.readById(userId)).willReturn(Optional.ofNullable(user));
        given(jwtHelper.createToken(any(User.class), any(TokenMode.class))).willReturn(tokenDto);

        // when
        OAuthTokenDto result = oAuthCommandService.signUp(userId, signUpDto);

        // then
        verify(jwtHelper).createToken(any(User.class), any(TokenMode.class));
        assertEquals(tokenDto.accessToken(), result.tokenDto().accessToken());
        assertEquals(tokenDto.refreshToken(), result.tokenDto().refreshToken());
        assertThat(result.isSignUp()).isTrue();
    }

    @Test
    @DisplayName("기존 회원 - OAuth 로그인 성공")
    void signInExistingUserSuccess() {
        // given
        given(oAuthClientFactory.getPlatformService(platform)).willReturn(oAuthClient);
        given(oAuthClient.authenticate("authCode123")).willReturn(userInfoRes);
        given(userService.readByUsernameIncludeDeleted(username)).willReturn(Optional.of(user));
        given(jwtHelper.createToken(user, TokenMode.FULL)).willReturn(tokenDto);

        // when
        OAuthTokenDto result = oAuthCommandService.signIn(platform, oAuthCode);

        // then
        assertThat(result.tokenDto().accessToken()).isEqualTo("access");
        assertThat(result.tokenDto().refreshToken()).isEqualTo("refresh");
        assertThat(result.isSignUp()).isTrue();
    }

    @Test
    @DisplayName("신규 회원 - OAuth 로그인 시 등록 후 토큰 발급")
    void signInNewUserRegisterAndLogin() {
        // given
        given(oAuthClientFactory.getPlatformService(platform)).willReturn(oAuthClient);
        given(oAuthClient.authenticate("authCode123")).willReturn(userInfoRes);
        given(userService.readByUsernameIncludeDeleted(username)).willReturn(Optional.empty());
        given(userService.save(any(User.class))).willReturn(user);
        given(jwtHelper.createToken(user, TokenMode.FULL)).willReturn(tokenDto);

        // when
        OAuthTokenDto result = oAuthCommandService.signIn(platform, oAuthCode);

        // then
        assertThat(result.tokenDto().accessToken()).isEqualTo("access");
        assertThat(result.tokenDto().refreshToken()).isEqualTo("refresh");
        assertThat(result.isSignUp()).isFalse();
    }

    @Test
    @DisplayName("로그인 실패 - Soft Deleted User")
    void signInFailBySoftDeletedUser() {
        // given
        User user = User.builder()
                .username(username)
                .build();
        ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now().minusDays(2));

        given(oAuthClientFactory.getPlatformService(platform)).willReturn(oAuthClient);
        given(oAuthClient.authenticate("authCode123")).willReturn(userInfoRes);
        given(userService.readByUsernameIncludeDeleted(username)).willReturn(Optional.of(user));

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                oAuthCommandService.signIn(platform, oAuthCode)
        );

        // then
        assertEquals(UserErrorType.DEACTIVATED, exception.getErrorType());
        verify(jwtHelper, never()).createToken(user, TokenMode.FULL);
    }
}

