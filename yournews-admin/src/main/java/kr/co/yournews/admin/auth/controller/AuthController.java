package kr.co.yournews.admin.auth.controller;

import kr.co.yournews.admin.auth.service.AuthService;
import kr.co.yournews.auth.dto.SignInDto;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody SignInDto signInDto) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        authService.signIn(signInDto)
                )
        );
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(@RequestHeader("Authorization") String accessToken) {
        authService.signOut(accessToken);
        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
