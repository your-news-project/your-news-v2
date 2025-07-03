package kr.co.yournews.admin.user.service;

import kr.co.yournews.admin.user.dto.UserWithdrawDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementCommandService {
    private final UserService userService;

    /**
     * 사용자 ban 메서드
     * - 사유와 함께 사용자 활동 상태 변경 (UserStatus.BANNED)
     *
     * @param userId          : Ban 대상 사용자 ID
     * @param userWithdrawDto : BAN 사유
     */
    @Transactional
    public void banUser(Long userId, UserWithdrawDto userWithdrawDto) {
        log.info("[ADMIN 사용자 BAN 처리 요청] userId: {}, reason: {}", userId, userWithdrawDto.reason());

        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        user.ban(userWithdrawDto.reason());
        log.info("[ADMIN 사용자 BAN 처리 완료] userId: {}, reason: {}", userId, userWithdrawDto.reason());
    }

    /**
     * 사용자 ban 해지 메서드
     * - 사용자 활동 상태 변경 (UserStatus.ACTIVE)
     *
     * @param userId : Ban 해지 대상 사용자 ID
     */
    @Transactional
    public void unbanUser(Long userId) {
        log.info("[ADMIN 사용자 BAN 해지 요청] userId: {}", userId);

        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        user.unban();
        log.info("[ADMIN 사용자 BAN 해지 완료] userId: {}", userId);
    }
}
