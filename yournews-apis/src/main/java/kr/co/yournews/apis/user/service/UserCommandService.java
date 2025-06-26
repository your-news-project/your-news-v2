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
    private final FcmTokenCommandService fcmTokenCommandService;

    /**
     * 비밀번호 재설정 메서드
     *
     * @param userId            : 사용자 PK값
     * @param updatePasswordDto : (현재 비밀번호, 새 비밀번호) dto
     * @throws CustomException NOT_FOUND : 사용자 존재하지 않음
     *                         NOT_MATCHED_PASSWORD : 현재 비밀번호 일치하지 않음
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
     * 닉네임 업데이트 메서드
     *
     * @param userId        : 사용자 pk
     * @param updateProfile : 닉네임 변경 요청 DTO
     * @throws CustomException NOT_FOUND : 사용자 존재하지 않음
     *                         EXIST_NICKNAME : 이미 존재하는 닉네임
     */
    @Transactional
    public void updateUserProfile(Long userId, UserReq.UpdateProfile updateProfile) {
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        if (userService.existsByNickname(updateProfile.nickname())) {
            throw new CustomException(UserErrorType.EXIST_NICKNAME);
        }

        user.updateInfo(updateProfile.nickname());
    }

    /**
     * 사용자 구독 상태 변경
     *
     * @param userId       : 사용자 pk
     * @param updateStatus : 구독 상태 변경 요청 DTO
     * @throws CustomException NOT_FOUND : 사용자 존재하지 않음
     */
    @Transactional
    public void updateSubStatus(Long userId, UserReq.UpdateStatus updateStatus) {
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        user.updateSubStatus(updateStatus.subStatus(), updateStatus.dailySubStatus());
    }

    /**
     * 사용자 삭제 메서드
     * - 사용자 FcmToken 삭제 후
     * - 사용자 정보 삭제
     *
     * @param userId : 사용자 PK값
     */
    @Transactional
    public void deleteUser(Long userId) {
        fcmTokenCommandService.deleteTokenByUserId(userId);
        userService.deleteById(userId);
    }
}
