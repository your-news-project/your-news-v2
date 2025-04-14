package kr.co.yournews.apis.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.co.yournews.apis.auth.service.AuthCommandService;
import kr.co.yournews.auth.dto.SignInDto;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.common.exception.AuthErrorType;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.common.response.success.SuccessResponse;
import kr.co.yournews.common.util.AuthConstants;
import kr.co.yournews.common.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthCommandService authCommandService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpDto.Auth sighUpDto) {
        return createTokenRes(authCommandService.signUp(sighUpDto));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid SignInDto signInDto) {
        return createTokenRes(authCommandService.signIn(signInDto));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        authCommandService.signOut(refreshToken, response);

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> accessTokenReissue(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new CustomException(AuthErrorType.REFRESH_TOKEN_NOT_FOUND);
        }

        return createTokenRes(authCommandService.reissueAccessToken(refreshToken));
    }

    private ResponseEntity<?> createTokenRes(TokenDto tokenDto) {

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("accessToken", tokenDto.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        CookieUtil.createCookie(
                                AuthConstants.REFRESH_TOKEN_KEY.getValue(),
                                tokenDto.refreshToken(),
                                Duration.ofDays(7).toSeconds()
                        ).toString())
                .body(SuccessResponse.from(responseData));
    }
}
