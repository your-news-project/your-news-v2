package kr.co.yournews.apis.subscription.controller;

import kr.co.yournews.apis.subscription.dto.SubscriptionDto;
import kr.co.yournews.apis.subscription.service.SubscriptionCommandService;
import kr.co.yournews.apis.subscription.service.SubscriptionQueryService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;

    @PostMapping("/apple/confirm")
    public ResponseEntity<?> validateAndCreateSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SubscriptionDto.AppleConfirmRequest request
    ) {
        subscriptionCommandService.validateAndCreateAppleSubscription(
                userDetails.getUserId(), request
        );

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PostMapping("/apple/restore")
    public ResponseEntity<?> restoreAppleSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SubscriptionDto.AppleRestoreRequest request
    ) {
        subscriptionCommandService.restoreAppleSubscription(
                userDetails.getUserId(), request
        );

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PostMapping("/google/confirm")
    public ResponseEntity<?> validateAndCreateGoogleSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SubscriptionDto.GoogleConfirmRequest request
    ) {
        subscriptionCommandService.validateAndCreateGoogleSubscription(
                userDetails.getUserId(), request
        );

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @GetMapping("/status")
    public ResponseEntity<?> getUserSubscriptionStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        subscriptionQueryService.getUserSubscriptionStatus(userDetails.getUserId())
                )
        );
    }
}
