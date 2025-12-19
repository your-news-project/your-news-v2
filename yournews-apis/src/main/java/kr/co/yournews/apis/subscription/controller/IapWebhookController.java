package kr.co.yournews.apis.subscription.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.yournews.apis.subscription.webhook.apple.AppleSubscriptionWebhookService;
import kr.co.yournews.apis.subscription.webhook.google.GoogleSubscriptionWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
public class IapWebhookController {
    private final AppleSubscriptionWebhookService appleSubscriptionWebhookService;
    private final GoogleSubscriptionWebhookService googleSubscriptionWebhookService;

    @PostMapping("/apple")
    public ResponseEntity<?> handleWebhook(@RequestBody String body) {
        appleSubscriptionWebhookService.handleAppleWebhook(body);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/google")
    public ResponseEntity<Void> handleGoogleWebhook(
            @RequestBody(required = false) String body,
            HttpServletRequest request
    ) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        googleSubscriptionWebhookService.handleGoogleWebhook(auth, body);

        return ResponseEntity.ok().build();
    }
}
