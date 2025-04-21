package kr.co.yournews.apis.auth.service;

import kr.co.yournews.apis.auth.dto.OAuthCode;
import kr.co.yournews.apis.auth.dto.OAuthTokenDto;
import kr.co.yournews.apis.auth.dto.UserStatusDto;
import kr.co.yournews.apis.news.service.SubNewsCommandService;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.auth.helper.JwtHelper;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.domain.user.type.OAuthPlatform;
import kr.co.yournews.domain.user.type.Role;
import kr.co.yournews.infra.oauth.OAuthClient;
import kr.co.yournews.infra.oauth.dto.OAuthUserInfoRes;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        user.updateInfo(signUpDto.nickname(), signUpDto.subStatus(), signUpDto.dailySubStatus());
        subNewsCommandService.subscribeToNews(user, signUpDto.newsIds(), signUpDto.keywords());

        return OAuthTokenDto.of(jwtHelper.createToken(user), user.isSignedUp());
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
        OAuthClient oAuthClient = oAuthClientFactory.getPlatformService(platform);

        OAuthUserInfoRes userInfoRes = oAuthClient.fetchUserInfoFromPlatform(oAuthCode.code());
        UserStatusDto userStatusDto = findOrRegisterUser(platform, userInfoRes);
        TokenDto tokenDto = jwtHelper.createToken(userStatusDto.user());

        return OAuthTokenDto.of(tokenDto, userStatusDto.isSignUp());
    }

    /**
     * 사용자 상태 확인 및 등록
     *
     * @param platform    : OAuth 플랫폼
     * @param userInfoRes : 플랫폼 사용자 정보
     * @return : 사용자 상태 정보
     */
    private UserStatusDto findOrRegisterUser(OAuthPlatform platform, OAuthUserInfoRes userInfoRes) {
        String username = userInfoRes.nickname() + "_" + userInfoRes.id();

        return userService.readByUsername(username)
                .map(user -> UserStatusDto.of(user, user.isSignedUp()))
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
                        .signedUp(false)
                        .role(Role.GUEST)
                        .build()
        );

        return UserStatusDto.of(user, false);
    }
}
