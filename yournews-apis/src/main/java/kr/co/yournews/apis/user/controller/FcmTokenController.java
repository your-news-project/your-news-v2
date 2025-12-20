package kr.co.yournews.apis.user.controller;

import jakarta.validation.Valid;
import kr.co.yournews.apis.user.dto.FcmTokenReq;
import kr.co.yournews.apis.user.service.FcmTokenCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fcm-token")
@RequiredArgsConstructor
public class FcmTokenController {
    private final FcmTokenCommandService fcmTokenCommandService;

    @PostMapping
    public ResponseEntity<?> registerToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid FcmTokenReq.Register registerDto
    ) {
        fcmTokenCommandService.registerFcmToken(userDetails.getUserId(), registerDto);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @DeleteMapping
    public ResponseEntity<?> deleteToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FcmTokenReq.Delete deleteDto
    ) {
        fcmTokenCommandService.deleteTokenByUserAndDevice(userDetails.getUserId(), deleteDto.deviceInfo());
        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
