package kr.co.yournews.apis.user.controller;

import jakarta.validation.Valid;
import kr.co.yournews.apis.user.dto.UserReq;
import kr.co.yournews.apis.user.service.UserCommandService;
import kr.co.yournews.apis.user.service.UserQueryService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @GetMapping
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        userQueryService.getUserInfoById(userDetails.getUserId())
                )
        );
    }

    @PatchMapping("/password")
    public ResponseEntity<?> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UserReq.UpdatePassword updatePasswordDto
    ) {
        userCommandService.updatePassword(userDetails.getUserId(), updatePasswordDto);

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userCommandService.deleteUser(userDetails.getUserId());

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PatchMapping
    public ResponseEntity<?> updateUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UserReq.UpdateProfile updateProfile
    ) {
        userCommandService.updateUserProfile(userDetails.getUserId(), updateProfile);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PatchMapping("/subscribe")
    public ResponseEntity<?> updateSubStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserReq.UpdateStatus updateStatus
    ) {
        userCommandService.updateSubStatus(userDetails.getUserId(), updateStatus);
        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
