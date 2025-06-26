package kr.co.yournews.admin.user.service;

import kr.co.yournews.admin.user.dto.UserWithdrawDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        user.ban(userWithdrawDto.reason());
    }

    /**
     * 사용자 ban 해지 메서드
     * - 사용자 활동 상태 변경 (UserStatus.ACTIVE)
     *
     * @param userId : Ban 해지 대상 사용자 ID
     */
    @Transactional
    public void unbanUser(Long userId) {
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        user.unban();
    }
}
