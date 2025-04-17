package kr.co.yournews.apis.auth.service;

import kr.co.yournews.apis.auth.dto.OAuthCode;
import kr.co.yournews.apis.auth.dto.OAuthTokenDto;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.auth.helper.JwtHelper;
import kr.co.yournews.domain.user.entity.User;
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

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

    @InjectMocks
    private OAuthCommandService oAuthCommandService;

    private final OAuthPlatform platform = OAuthPlatform.NAVER;
    private final OAuthCode oAuthCode = new OAuthCode("authCode123");

    private final OAuthUserInfoRes userInfoRes = new OAuthUserInfoRes("oauthId123", "nickname", "test@naver.com");

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
        SignUpDto.OAuth signUpDto = new SignUpDto.OAuth("test");
        Long userId = 1L;

        given(userService.readById(userId)).willReturn(Optional.ofNullable(user));
        given(jwtHelper.createToken(any(User.class))).willReturn(tokenDto);

        // when
        OAuthTokenDto result = oAuthCommandService.signUp(userId, signUpDto);

        // then
        verify(jwtHelper).createToken(any(User.class));
        assertEquals(tokenDto.accessToken(), result.tokenDto().accessToken());
        assertEquals(tokenDto.refreshToken(), result.tokenDto().refreshToken());
        assertThat(result.isSignUp()).isTrue();
    }

    @Test
    @DisplayName("기존 회원 - OAuth 로그인 성공")
    void signInExistingUserSuccess() {
        // given
        given(oAuthClientFactory.getPlatformService(platform)).willReturn(oAuthClient);
        given(oAuthClient.fetchUserInfoFromPlatform("authCode123")).willReturn(userInfoRes);
        given(userService.readByUsername("nickname_oauthId123")).willReturn(Optional.of(user));
        given(jwtHelper.createToken(user)).willReturn(tokenDto);

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
        given(oAuthClient.fetchUserInfoFromPlatform("authCode123")).willReturn(userInfoRes);
        given(userService.readByUsername("nickname_oauthId123")).willReturn(Optional.empty());
        given(userService.save(any(User.class))).willReturn(user);
        given(jwtHelper.createToken(user)).willReturn(tokenDto);

        // when
        OAuthTokenDto result = oAuthCommandService.signIn(platform, oAuthCode);

        // then
        assertThat(result.tokenDto().accessToken()).isEqualTo("access");
        assertThat(result.tokenDto().refreshToken()).isEqualTo("refresh");
        assertThat(result.isSignUp()).isFalse();
    }
}

