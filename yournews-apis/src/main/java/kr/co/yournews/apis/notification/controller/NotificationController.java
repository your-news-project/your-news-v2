package kr.co.yournews.apis.notification.controller;

import kr.co.yournews.apis.notification.dto.NotificationDto;
import kr.co.yournews.apis.notification.service.NotificationCommandService;
import kr.co.yournews.apis.notification.service.NotificationQueryService;
import kr.co.yournews.apis.notification.service.NotificationRankingService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifies")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;
    private final NotificationRankingService notificationRankingService;

    @GetMapping("/id/{notificationId}")
    public ResponseEntity<?> getNotificationById(@PathVariable Long notificationId) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        notificationQueryService.getNotificationById(notificationId)
                )
        );
    }

    @GetMapping("/public-id/{publicId}")
    public ResponseEntity<?> getNotificationByPublicId(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @PathVariable String publicId) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        notificationQueryService.getNotificationByPublicId(userDetails.getUserId(), publicId)
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam boolean isRead,
            @RequestParam(required = false) String newsName,
            @RequestParam(required = false, defaultValue = "false") boolean others
    ) {

        Long userId = userDetails.getUserId();
        Page<NotificationDto.Summary> result;

        if (others) {
            result = notificationQueryService.getNotificationsByUserIdAndNewsNameNotInAndIsRead(userId, isRead, pageable);
        } else if (newsName != null && !newsName.isBlank()) {
            result = notificationQueryService.getNotificationsByUserIdAndNewsNameAndIsRead(userId, newsName, isRead, pageable);
        } else {
            result = notificationQueryService.getNotificationsByUserIdAndIsRead(userId, isRead, pageable);
        }

        return ResponseEntity.ok(SuccessResponse.from(result));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        notificationQueryService.getUnreadCount(userDetails.getUserId())
                )
        );
    }

    @GetMapping("/rank")
    public ResponseEntity<?> getTopNewsRanking() {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        notificationRankingService.getNewsRanking()
                )
        );
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @PathVariable Long notificationId) {
        notificationCommandService.deleteNotification(userDetails.getUserId(), notificationId);

        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
