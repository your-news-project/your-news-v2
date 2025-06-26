package kr.co.yournews.admin.user.controller;

import jakarta.validation.Valid;
import kr.co.yournews.admin.user.dto.UserWithdrawDto;
import kr.co.yournews.admin.user.service.UserManagementCommandService;
import kr.co.yournews.admin.user.service.UserManagementQueryService;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserManagementController {
    private final UserManagementCommandService userManagementCommandService;
    private final UserManagementQueryService userManagementQueryService;

    @PatchMapping("/{userId}/ban")
    public ResponseEntity<?> banUser(
            @PathVariable Long userId,
            @RequestBody @Valid UserWithdrawDto userWithdrawDto
    ) {
        userManagementCommandService.banUser(userId, userWithdrawDto);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PatchMapping("/{userId}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable Long userId) {
        userManagementCommandService.unbanUser(userId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @GetMapping
    public ResponseEntity<?> findAllUsers(
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(userManagementQueryService.findAllUsers(pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> findUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userManagementQueryService.findUserById(userId));
    }
}
