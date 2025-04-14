package kr.co.yournews.auth.helper;

import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.auth.jwt.provider.JwtProvider;
import kr.co.yournews.auth.service.RefreshTokenService;
import kr.co.yournews.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class JwtHelperTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private JwtHelper jwtHelper;

    private User user;
    private String username;
    private String nickname;
    private Long userId;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("test")
                .nickname("테스터")
                .build();

        ReflectionTestUtils.setField(user, "id", 1L);

        this.username = "test";
        this.nickname = "테스터";
        this.userId = 1L;
    }

    @Test
    @DisplayName("AccessToekn, RefreshToken 생성 테스트")
    void createTokenTest() {
        // given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        TokenDto tokenDto = TokenDto.of(accessToken, refreshToken);

        given(jwtProvider.generateAccessToken(username, nickname, userId)).willReturn(accessToken);
        given(jwtProvider.generateRefreshToken(username, nickname, userId)).willReturn(refreshToken);

        // when
        TokenDto returnTokenDto = jwtHelper.createToken(user);

        // then
        verify(refreshTokenService, times(1)).saveRefreshToken(username, refreshToken);
        assertEquals(tokenDto.accessToken(), returnTokenDto.accessToken());
        assertEquals(tokenDto.refreshToken(), returnTokenDto.refreshToken());
    }
}
