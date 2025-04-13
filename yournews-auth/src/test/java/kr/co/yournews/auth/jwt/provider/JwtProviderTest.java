package kr.co.yournews.auth.jwt.provider;

import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class JwtProviderTest {
    private final String secretKey = "S3cr3tK3yForJwt_T0ken_Gen3ration_ThisKeyIsDefinitelyLongEnough_398fhsduh238dhf28";
    private JwtProvider jwtProvider;
    private String username;
    private String nickname;
    private Long userId;

    @BeforeEach
    void setUp() {
        this.jwtProvider = new JwtProvider(secretKey, 60000, 120000, "test");
        this.username = "test";
        this.nickname = "테스터";
        this.userId = 1L;
    }

    @Test
    @DisplayName("토큰 생성 테스트")
    void createToken() {
        // when
        String accessToken = jwtProvider.generateAccessToken(username, nickname, userId);
        String refreshToken = jwtProvider.generateRefreshToken(username, nickname, userId);

        // then
        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        System.out.println("[accessToken] : " + accessToken +
                "\n[refreshToken] : " + refreshToken);
    }


    @Test
    @DisplayName("토큰 정보 출력 테스트")
    void getInfoFromToken() {
        // given
        String token = jwtProvider.generateAccessToken(username, nickname, userId);

        // when
        String jwtUsername = jwtProvider.getUsername(token);
        String jwtNickname = jwtProvider.getNickname(token);
        Long jwtUserId = jwtProvider.getUserId(token);

        // then
        assertNotNull(jwtUsername);
        assertNotNull(jwtNickname);
        assertNotNull(jwtUserId);

        System.out.println("[username] : " + jwtUsername +
                "\n[nickname] : " + jwtNickname +
                "\n[userId] : " + jwtUserId);
    }

    @Test
    @DisplayName("토큰의 만료일 테스트")
    void getExpiryDate() {
        // given
        String token = jwtProvider.generateAccessToken(username, nickname, userId);

        // when
        jwtProvider.getExpiryDate(token);

        // then
        assertNotNull(jwtProvider.getExpiryDate(token));
        System.out.println(jwtProvider.getExpiryDate(token));
    }

    @Test
    @DisplayName("토큰의 만료 테스트")
    void isTokenExpired() {
        // given
        String accessToken = jwtProvider.generateAccessToken(username, nickname, userId);

        // when

        // then
        assertFalse(jwtProvider.isExpired(accessToken));
    }

    @Test
    @DisplayName("서명 올바르지 않다면, 예외 발생")
    void getClaimsFromTokenWithInvalidSignature() {
        // given
        String accessToken = jwtProvider.generateAccessToken(username, nickname, userId);
        String invalidToken = accessToken + "invalid";

        // when & then
        assertThrows(SignatureException.class, () -> jwtProvider.getUsername(invalidToken));
    }
}
