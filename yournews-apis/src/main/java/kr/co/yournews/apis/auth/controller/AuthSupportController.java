package kr.co.yournews.apis.auth.controller;

import kr.co.yournews.apis.auth.dto.AuthCodeDto;
import kr.co.yournews.apis.auth.service.mail.AuthCodeManager;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthSupportController {
    private final AuthCodeManager authCodeManager;

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
}
