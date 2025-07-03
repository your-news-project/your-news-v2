package kr.co.yournews.admin.auth.service;

import kr.co.yournews.auth.dto.SignInDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.auth.helper.JwtHelper;
import kr.co.yournews.auth.helper.TokenMode;
import kr.co.yournews.auth.service.PasswordEncodeService;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static kr.co.yournews.common.util.AuthConstants.TOKEN_TYPE;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncodeService passwordEncodeService;
    private final JwtHelper jwtHelper;

    /**
     * 서비스 이용을 위한 로그인 메서드
     * - 비밀번호 일치 여부를 검증한 뒤 정상 사용자에게 토큰을 발급
     *
     * @param signInDto : 사용자가 입력한 정보
     * @return : jwt token
     */
    @Transactional(readOnly = true)
    public TokenDto signIn(SignInDto signInDto) {
        log.info("[ADMIN 로그인 요청] username: {}", signInDto.username());

        User user = userService.readByUsername(signInDto.username())
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        if (!passwordEncodeService.matches(signInDto.password(), user.getPassword())) {
            throw new CustomException(UserErrorType.NOT_MATCHED_PASSWORD);
        }

        if (!user.isAdmin()) {
            throw new CustomException(UserErrorType.NOT_ADMIN);
        }

        log.info("[ADMIN 로그인 성공] userId: {}", user.getId());
        return jwtHelper.createToken(user, TokenMode.ACCESS_ONLY);
    }

    /**
     * 서비스 로그아웃 메서드
     *
     * @param accessTokenInHeader : 헤더에 있는 accessToken
     */
    public void signOut(String accessTokenInHeader) {
        String accessToken = accessTokenInHeader.substring(TOKEN_TYPE.length()).trim();
        jwtHelper.removeToken(accessToken);

        log.info("[ADMIN 로그아웃 성공]");
    }
}
