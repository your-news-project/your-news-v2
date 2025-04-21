package kr.co.yournews.apis.auth.controller;

import jakarta.validation.Valid;
import kr.co.yournews.apis.auth.dto.AuthCodeDto;
import kr.co.yournews.apis.auth.dto.PassResetDto;
import kr.co.yournews.apis.auth.dto.UsernameDto;
import kr.co.yournews.apis.auth.service.UserValidationService;
import kr.co.yournews.apis.auth.service.UsernameFindService;
import kr.co.yournews.apis.auth.service.mail.AuthCodeManager;
import kr.co.yournews.apis.auth.service.mail.PassCodeManager;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthSupportController {
    private final AuthCodeManager authCodeManager;
    private final PassCodeManager passCodeManager;
    private final UsernameFindService usernameFindService;
    private final UserValidationService userValidationService;

    @PostMapping("/code/request")
    public ResponseEntity<?> requestCode(@RequestBody AuthCodeDto.Request dto) {
        authCodeManager.sendAuthCode(dto);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PostMapping("/code/verify")
    public ResponseEntity<?> verifyCode(@RequestBody AuthCodeDto.Verify dto) {
        authCodeManager.verifyAuthCode(dto);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PostMapping("/password/request")
    public ResponseEntity<?> requestResetLink(@RequestBody PassResetDto.VerifyUser verifyUserDto) {
        passCodeManager.initiatePasswordReset(verifyUserDto);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> applyNewPassword(@RequestBody @Valid PassResetDto.ResetPassword resetPasswordDto) {
        passCodeManager.applyNewPassword(resetPasswordDto);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PostMapping("/username/retrieve")
    public ResponseEntity<?> findUsername(@RequestBody @Valid UsernameDto.Request dto) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        usernameFindService.getUsernameByEmail(dto)
                )
        );
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String value) {
        return ResponseEntity.ok(SuccessResponse.from(userValidationService.isUsernameExists(value)));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam String value) {
        return ResponseEntity.ok(SuccessResponse.from(userValidationService.isNicknameExists(value)));
    }
}
