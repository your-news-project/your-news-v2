package kr.co.yournews.apis.auth.service;

import kr.co.yournews.apis.auth.dto.OAuthCode;
import kr.co.yournews.apis.auth.dto.OAuthTokenDto;
import kr.co.yournews.apis.auth.dto.RestoreUserDto;
import kr.co.yournews.apis.auth.dto.UserStatusDto;
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
import kr.co.yournews.domain.user.type.Role;
import kr.co.yournews.domain.user.type.UserStatus;
import kr.co.yournews.infra.oauth.OAuthClient;
import kr.co.yournews.infra.oauth.dto.OAuthUserInfoRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthCommandService {
    private final UserService userService;
    private final OAuthClientFactory oAuthClientFactory;
    private final JwtHelper jwtHelper;
    private final SubNewsCommandService subNewsCommandService;

    /**
     * OAuth 추가 정보 입력 회원가입 진행 메서드
     *
     * @param userId    : 사용자 pk값
     * @param signUpDto : 사용자 회원가입 요청 dto
     * @return : jwt token
     */
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public OAuthTokenDto signUp(Long userId, SignUpDto.OAuth signUpDto) {
        log.info("[OAuth 회원가입 요청] userId: {}, nickname: {}", userId, signUpDto.nickname());

        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        user.updateInfo(signUpDto.nickname(), signUpDto.subStatus(), signUpDto.dailySubStatus());
        subNewsCommandService.subscribeToNews(user, signUpDto.newsIds(), signUpDto.keywords());

        log.info("[OAuth 회원가입 완료] userId: {}, signedUp: {}", userId, user.isSignedUp());
        return OAuthTokenDto.of(jwtHelper.createToken(user, TokenMode.FULL), user.isSignedUp());
    }

    /**
     * 서비스 로그인을 위한 메서드
     *
     * @param platform  : 로그인 플랫폼
     * @param oAuthCode : 플랫폼의 인가 코드
     * @return : jwt token, 최초 가입 여부
     */
    @Transactional
    public OAuthTokenDto signIn(OAuthPlatform platform, OAuthCode oAuthCode) {
        log.info("[OAuth 로그인 요청] platform: {}, code: {}", platform.name(), oAuthCode.code());

        OAuthClient oAuthClient = oAuthClientFactory.getPlatformService(platform);
        OAuthUserInfoRes userInfoRes = oAuthClient.authenticate(oAuthCode.code());

        log.info("[OAuth 인증 완료] platform: {}, email: {}", platform.name(), userInfoRes.email());

        UserStatusDto userStatusDto = findOrRegisterUser(platform, userInfoRes);
        TokenDto tokenDto = jwtHelper.createToken(userStatusDto.user(), TokenMode.FULL);

        log.info("[OAuth 로그인 완료] userId: {}, signedUp: {}", userStatusDto.user().getId(), userStatusDto.isSignUp());
        return OAuthTokenDto.of(tokenDto, userStatusDto.isSignUp());
    }

    /**
     * 사용자 상태 확인 및 등록
     * - 가입된 사용자가 소프트 딜리트 상태라면 복구 안내 에러를 발생
     * - 정상 사용자라면 사용자 정보를 반환
     * - BAN 여부 확인
     * - 존재하지 않는 경우 신규 사용자를 등록하고 반환
     *
     * @param platform    : OAuth 플랫폼
     * @param userInfoRes : 플랫폼 사용자 정보
     * @return : 사용자 상태 정보
     */
    private UserStatusDto findOrRegisterUser(OAuthPlatform platform, OAuthUserInfoRes userInfoRes) {
        String username = platform.name().toLowerCase() + "_" + userInfoRes.id();

        return userService.readByUsernameIncludeDeleted(username)
                .map(user -> {
                    if (user.isBanned()) {
                        throw new CustomException(UserErrorType.BANNED);
                    }

                    if (user.isDeleted()) {
                        throw new CustomException(
                                UserErrorType.DEACTIVATED,
                                RestoreUserDto.OAuthErrorData.of(user.getUsername(), user.getDeletedAt())
                        );
                    }
                    return UserStatusDto.of(user, user.isSignedUp());
                })
                .orElseGet(() -> registerNewUser(platform, username, userInfoRes.email()));
    }

    /**
     * 신규 사용자 등록
     *
     * @param platform : OAuth 플랫폼
     * @param username : 사용자 이름
     * @param email    : 사용자 이메일
     * @return : 등록된 사용자
     */
    private UserStatusDto registerNewUser(OAuthPlatform platform, String username, String email) {
        User user = userService.save(
                User.builder()
                        .username(username)
                        .nickname(username)
                        .email(email)
                        .platform(platform)
                        .status(UserStatus.ACTIVE)
                        .signedUp(false)
                        .role(Role.GUEST)
                        .build()
        );

        log.info("[OAuth 신규 사용자 등록 완료] userId: {}, username: {}", user.getId(), username);
        return UserStatusDto.of(user, false);
    }
}
