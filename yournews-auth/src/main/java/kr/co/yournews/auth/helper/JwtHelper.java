package kr.co.yournews.auth.helper;

import jakarta.servlet.http.HttpServletResponse;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.auth.jwt.provider.JwtProvider;
import kr.co.yournews.auth.service.RefreshTokenService;
import kr.co.yournews.auth.service.TokenBlackListService;
import kr.co.yournews.common.exception.AuthErrorType;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.common.util.AuthConstants;
import kr.co.yournews.common.util.CookieUtil;
import kr.co.yournews.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtHelper {
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlackListService tokenBlackListService;

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

    /**
     * refreshToken 기반으로 accessToken, refreshToken 재발급
     *
     * @param refreshToken : 클라이언트에서 받은 refreshToken (쿠키 등)
     * @return : 재발급된 토큰을 담은 TokenDto
     */
    public TokenDto reissueToken(String refreshToken) {
        String username = jwtProvider.getUsername(refreshToken);

        if (!refreshTokenService.existedRefreshToken(username))
            throw new CustomException(AuthErrorType.REFRESH_TOKEN_NOT_FOUND);

        String nickname = jwtProvider.getNickname(refreshToken);
        Long userId = jwtProvider.getUserId(refreshToken);

        String newAccessToken = jwtProvider.generateAccessToken(username, nickname, userId);
        String newRefreshToken = jwtProvider.generateRefreshToken(username, nickname, userId);

        refreshTokenService.saveRefreshToken(username, newRefreshToken);

        return TokenDto.of(newAccessToken, newRefreshToken);
    }

    /**
     * 로그아웃 시 refreshToken 제거 메서드 (외부에서 호출)
     * - accessToken은 블랙리스트 등록
     * - refreshToken은 쿠키 제거 + Redis에서 삭제
     *
     * @param accessToken  : 헤더에 담긴 JWT access token
     * @param refreshToken : 쿠키에 저장된 refreshToken
     * @param response     : 쿠키 제거용 HttpServletResponse
     */
    public void removeToken(String accessToken, String refreshToken, HttpServletResponse response) {
        deleteAccessToken(accessToken);
        deleteRefreshToken(refreshToken, response);
    }

    /**
     * accessToken을 블랙리스트에 등록 (로그아웃 처리용)
     * - 해당 accessToken은 더 이상 사용할 수 없도록 차단
     * - Redis에 TTL 기반으로 저장됨 (토큰 만료 시 자동 삭제)
     *
     * @param accessToken : 헤더에 담긴 JWT access token
     */
    private void deleteAccessToken(String accessToken) {
        LocalDateTime accessTokenExpireAt = jwtProvider.getExpiryDate(accessToken);
        tokenBlackListService.saveBlackList(accessToken, accessTokenExpireAt);
    }

    /**
     * refreshToken 제거 내부 메서드
     * - 쿠키 삭제 + 저장소(예: Redis)에서 refreshToken 제거
     *
     * @param refreshToken : 클라이언트에서 전달받은 refreshToken
     * @param response     : 쿠키 삭제를 위한 응답 객체
     */
    private void deleteRefreshToken(String refreshToken, HttpServletResponse response) {
        String username = jwtProvider.getUsername(refreshToken);
        CookieUtil.deleteCookie(AuthConstants.REFRESH_TOKEN_KEY.getValue(), response);
        refreshTokenService.deleteRefreshToken(username);
    }
}
