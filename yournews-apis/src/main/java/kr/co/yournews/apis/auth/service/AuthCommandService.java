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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static kr.co.yournews.common.util.AuthConstants.TOKEN_TYPE;

@Service
@RequiredArgsConstructor
public class AuthCommandService {
    private final UserService userService;
    private final PasswordEncodeService passwordEncodeService;
    private final JwtHelper jwtHelper;
    private final AuthCodeService authCodeService;
    private final SubNewsCommandService subNewsCommandService;
    private final FcmTokenCommandService fcmTokenCommandService;

    /**
     * dto를 통해 비밀번호 인코딩 후, 회원가입 진행 메서드.
     * - 인증된 이메일 확인 검증 로직 (외부 api 플랫폼을 통한 회원가입을 막기 위해)
     *
     * @param signUpDto : 사용자 회원가입 요청 dto
     * @return : jwt token
     */
    @Transactional
    public TokenDto signUp(SignUpDto.Auth signUpDto) {
        authCodeService.ensureVerifiedAndConsume(signUpDto.email());

        String encodedPassword = passwordEncodeService.encode(signUpDto.password());
        User user = signUpDto.toEntity(encodedPassword);
        userService.save(user);

        subNewsCommandService.subscribeToNews(user, signUpDto.newsIds(), signUpDto.keywords());
        return jwtHelper.createToken(user, TokenMode.FULL);
    }

    /**
     * 서비스 이용을 위한 로그인 메서드
     *
     * - 이미 가입된 사용자가 소프트 딜리트 상태라면 복구 안내 에러를 발생
     * - 비밀번호 일치 여부를 검증한 뒤 정상 사용자에게 토큰을 발급
     *
     * @param signInDto : 사용자가 입력한 정보
     * @return : jwt token
     */
    @Transactional(readOnly = true)
    public TokenDto signIn(SignInDto signInDto) {
        User user = userService.readByUsernameIncludeDeleted(signInDto.username())
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        if (!passwordEncodeService.matches(signInDto.password(), user.getPassword())) {
            throw new CustomException(UserErrorType.NOT_MATCHED_PASSWORD);
        }

        if (user.isDeleted()) {
            throw new CustomException(
                    UserErrorType.DEACTIVATED,
                    RestoreUserDto.AuthErrorData.of(user.getDeletedAt())
            );
        }

        return jwtHelper.createToken(user, TokenMode.FULL);
    }

    /**
     * 소프트 딜리트된 사용자를 복구하는 메서드
     *
     * - username 기준으로 삭제된 사용자를 조회하고 복구
     * - 복구가 완료되면 바로 JWT 토큰을 발급해 로그인 처리 진행
     * - 이미 활성화된 사용자는 복구할 수 없음.
     *
     * @param restoreRequest : 복구 요청 정보 (username)
     * @return : jwt token
     */
    @Transactional
    public TokenDto restoreUser(RestoreUserDto.Request restoreRequest) {
        User user = userService.readByUsernameIncludeDeleted(restoreRequest.username())
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        if (!user.isDeleted()) {
            throw new CustomException(UserErrorType.ALREADY_ACTIVE);
        }

        user.restore();
        return jwtHelper.createToken(user, TokenMode.FULL);
    }

    /**
     * access token 재발급 메서드
     *
     * @param refreshToken : access token 재발급을 위한 refresh token
     * @return : jwt token
     */
    public TokenDto reissueAccessToken(String refreshToken) {
        return jwtHelper.reissueToken(refreshToken);
    }

    /**
     * 서비스 로그아웃 메서드
     *
     * @param accessTokenInHeader : 헤더에 있는 accessToken
     * @param refreshToken        : refresh token
     */
    public void signOut(
            String accessTokenInHeader,
            String refreshToken,
            Long userId,
            SignOutDto signOutDto
    ) {
        fcmTokenCommandService.deleteTokenByUserAndDevice(userId, signOutDto.deviceInfo());
        String accessToken = accessTokenInHeader.substring(TOKEN_TYPE.length()).trim();
        jwtHelper.removeToken(accessToken, refreshToken);
    }
}
