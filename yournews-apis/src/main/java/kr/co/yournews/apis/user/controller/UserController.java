package kr.co.yournews.apis.user.controller;

import jakarta.validation.Valid;
import kr.co.yournews.apis.user.dto.UserReq;
import kr.co.yournews.apis.user.service.UserCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserCommandService userCommandService;

    @PatchMapping("/password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody @Valid UserReq.UpdatePassword updatePasswordDto) {
        userCommandService.updatePassword(userDetails.getUserId(), updatePasswordDto);

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userCommandService.deleteUser(userDetails.getUserId());

        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
