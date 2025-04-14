package kr.co.yournews.auth.helper;

import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.auth.jwt.provider.JwtProvider;
import kr.co.yournews.auth.service.RefreshTokenService;
import kr.co.yournews.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtHelper {
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * accessToken, refreshToken 생성
     *
     * @param user : 사용자 정보
     * @return : token을 담은 TokenDto
     */
    public TokenDto createToken(User user) {
        Long userId = user.getId();
        String username = user.getUsername();
        String nickname = user.getNickname();

        String accessToken = jwtProvider.generateAccessToken(username, nickname, userId);
        String refreshToken = jwtProvider.generateRefreshToken(username, nickname, userId);

        refreshTokenService.saveRefreshToken(username, refreshToken);

        return TokenDto.of(accessToken, refreshToken);
    }
}
