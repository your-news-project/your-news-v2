package kr.co.yournews.apis.user.service;

import kr.co.yournews.apis.user.dto.FcmTokenReq;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenCommandService {
    private final FcmTokenService fcmTokenService;
    private final UserService userService;

    /**
     * 사용자와 디바이스 정보를 기반으로 FCM 토큰을 등록하거나 업데이트하는 메서드
     * - 이미 존재하는 경우 토큰 값을 갱신하고, 존재하지 않으면 새로 저장
     *
     * @param userId      : 사용자 pk
     * @param registerDto : FCM 토큰 및 디바이스 정보 DTO
     * @throws CustomException NOT_FOUND :사용자 정보가 존재하지 않을 경우 예외 발생
     */
    @Transactional
    public void registerFcmToken(Long userId, FcmTokenReq.Register registerDto) {
        Optional<FcmToken> fcmToken = fcmTokenService.readByUserIdAndDeviceInfo(userId, registerDto.deviceInfo());

        if (fcmToken.isPresent()) {
            fcmToken.get().updateToken(registerDto.token());
            log.info("[FCM 토큰 갱신 성공] userId: {}, deviceInfo: {}", userId, registerDto.deviceInfo());
        } else {
            User user = userService.readById(userId)
                    .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

            fcmTokenService.save(registerDto.toEntity(user));
            log.info("[FCM 토큰 등록 완료] userId: {}, deviceInfo: {}", userId, registerDto.deviceInfo());
        }
    }

    /**
     * 특정 사용자와 디바이스 정보를 기준으로 해당 FCM 토큰을 삭제하는 메서드
     * - 로그아웃 시, 사용
     *
     * @param userId     : 사용자 pk
     * @param deviceInfo : 디바이스 정보
     */
    @Transactional
    public void deleteTokenByUserAndDevice(Long userId, String deviceInfo) {
        fcmTokenService.deleteByUserIdAndDeviceInfo(userId, deviceInfo);
        log.info("[FCM 토큰 삭제] userId: {}, deviceInfo: {}", userId, deviceInfo);
    }

    /**
     * 사용자의 모든 FCM 토큰을 일괄 삭제합니다.
     * - 회원 탈퇴 시, 호출하여 사용됩니다.
     *
     * @param userId : 사용자 pk
     */
    @Transactional
    public void deleteTokenByUserId(Long userId) {
        fcmTokenService.deleteAllByUserId(userId);
        log.info("[FCM 토큰 전체 삭제] userId: {}", userId);
    }
}
