package kr.co.yournews.apis.auth.controller;

import jakarta.validation.Valid;
import kr.co.yournews.apis.auth.service.AuthCommandService;
import kr.co.yournews.auth.dto.SignInDto;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.common.exception.AuthErrorType;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {
        authCommandService.signOut(accessToken, refreshToken);

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> accessTokenReissue(
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new CustomException(AuthErrorType.REFRESH_TOKEN_NOT_FOUND);
        }

        return createTokenRes(authCommandService.reissueAccessToken(refreshToken));
    }

    private ResponseEntity<?> createTokenRes(TokenDto tokenDto) {
        return ResponseEntity.ok(SuccessResponse.from(tokenDto));
    }
}
