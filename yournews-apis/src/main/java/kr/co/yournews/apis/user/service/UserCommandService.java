package kr.co.yournews.apis.user.service;

import kr.co.yournews.apis.user.dto.UserReq;
import kr.co.yournews.auth.service.PasswordEncodeService;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCommandService {
    private final UserService userService;
    private final PasswordEncodeService passwordEncodeService;

    /**
     * 비밀번호 재설정 메서드
     *
     * @param userId            : 사용자 PK값
     * @param updatePasswordDto : (현재 비밀번호, 새 비밀번호) dto
     */
    @Transactional
    public void updatePassword(Long userId, UserReq.UpdatePassword updatePasswordDto) {
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        if (!passwordEncodeService.matches(updatePasswordDto.currentPassword(), user.getPassword())) {
            throw new CustomException(UserErrorType.NOT_MATCHED_PASSWORD);
        }

        user.updatePassword(passwordEncodeService.encode(updatePasswordDto.newPassword()));
    }

    /**
     * 사용자 삭제 메서드
     *
     * @param userId : 사용자 PK값
     */
    @Transactional
    public void deleteUser(Long userId) {
        userService.deleteById(userId);
    }
}
