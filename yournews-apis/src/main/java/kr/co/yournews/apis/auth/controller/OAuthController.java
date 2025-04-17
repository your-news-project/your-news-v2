package kr.co.yournews.apis.auth.controller;

import jakarta.validation.Valid;
import kr.co.yournews.apis.auth.dto.OAuthCode;
import kr.co.yournews.apis.auth.dto.OAuthTokenDto;
import kr.co.yournews.apis.auth.service.OAuthCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.common.response.success.SuccessResponse;
import kr.co.yournews.common.util.CookieUtil;
import kr.co.yournews.domain.user.type.OAuthPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static kr.co.yournews.common.util.AuthConstants.REFRESH_TOKEN_KEY;

@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class OAuthController {
    private final OAuthCommandService oAuthCommandService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestBody @Valid SignUpDto.OAuth sighUpDto) {
        return createTokenRes(oAuthCommandService.signUp(userDetails.getUserId(), sighUpDto));
    }

    @PostMapping("/sign-in/{platform}")
    public ResponseEntity<?> signIn(@PathVariable OAuthPlatform platform,
                                    @RequestBody OAuthCode oAuthCode) {

        return createTokenRes(oAuthCommandService.signIn(platform, oAuthCode));
    }

    private ResponseEntity<?> createTokenRes(OAuthTokenDto oAuthTokenDto) {
        Map<String, Object> responseData = new HashMap<>();

        TokenDto tokenDto = oAuthTokenDto.tokenDto();

        responseData.put("accessToken", tokenDto.accessToken());
        responseData.put("isSignUp", oAuthTokenDto.isSignUp());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        CookieUtil.createCookie(
                                REFRESH_TOKEN_KEY,
                                tokenDto.refreshToken(),
                                Duration.ofDays(7).toSeconds()
                        ).toString())
                .body(SuccessResponse.from(responseData));
    }
}
