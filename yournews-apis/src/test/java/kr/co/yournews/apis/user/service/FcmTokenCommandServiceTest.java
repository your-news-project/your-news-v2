package kr.co.yournews.apis.user.service;

import kr.co.yournews.apis.user.dto.FcmTokenReq;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class FcmTokenCommandServiceTest {

    @Mock
    private FcmTokenService fcmTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private FcmTokenCommandService fcmTokenCommandService;

    private final Long userId = 1L;
    private final String token = "testToken";
    private final String deviceInfo = "iPhone15";

    @Nested
    @DisplayName("FCM 토큰 등록")
    class RegisterFcmTokenTest {

        @Test
        @DisplayName("성공 - 기존 디바이스가 존재할 경우 토큰만 업데이트")
        void registerFcmTokenUpdate() {
            // given
            FcmToken existingToken = mock(FcmToken.class);
            given(fcmTokenService.readByUserIdAndDeviceInfo(userId, deviceInfo))
                    .willReturn(Optional.of(existingToken));

            FcmTokenReq.Register dto = new FcmTokenReq.Register(token, deviceInfo);

            // when
            fcmTokenCommandService.registerFcmToken(userId, dto);

            // then
            verify(existingToken, times(1)).updateToken(token);
            verify(fcmTokenService, never()).save(any());
        }

        @Test
        @DisplayName("성공 - 기존 디바이스가 없을 경우 새로 저장")
        void registerFcmTokenSaveNew() {
            // given
            given(fcmTokenService.readByUserIdAndDeviceInfo(userId, deviceInfo))
                    .willReturn(Optional.empty());

            User user = mock(User.class);
            given(userService.readById(userId)).willReturn(Optional.of(user));

            FcmTokenReq.Register dto = new FcmTokenReq.Register(token, deviceInfo);

            // when
            fcmTokenCommandService.registerFcmToken(userId, dto);

            // then
            verify(fcmTokenService).save(any(FcmToken.class));
        }

        @Test
        @DisplayName("실패 - 사용자 정보 없음")
        void registerFcmTokenUserNotFound() {
            // given
            given(fcmTokenService.readByUserIdAndDeviceInfo(userId, deviceInfo))
                    .willReturn(Optional.empty());

            given(userService.readById(userId)).willReturn(Optional.empty());

            FcmTokenReq.Register dto = new FcmTokenReq.Register(token, deviceInfo);

            // when
            CustomException exception = assertThrows(CustomException.class, () ->
                    fcmTokenCommandService.registerFcmToken(userId, dto));

            // then
            assertEquals(UserErrorType.NOT_FOUND, exception.getErrorType());
        }
    }

    @Nested
    @DisplayName("FCM 토큰 삭제")
    class DeleteFcmTokenTest {

        @Test
        @DisplayName("성공 - 디바이스 정보 기반 FCM 토큰 삭제")
        void deleteTokenByUserAndDevice() {
            // given
            FcmTokenReq.Delete dto = new FcmTokenReq.Delete(deviceInfo);

            // when
            fcmTokenCommandService.deleteTokenByUserAndDevice(userId, dto);

            // then
            verify(fcmTokenService).deleteByUserIdAndDeviceInfo(userId, deviceInfo);
        }

        @Test
        @DisplayName("성공 - 사용자 기반 모든 토큰 삭제")
        void deleteTokenByUserId() {
            // when
            fcmTokenCommandService.deleteTokenByUserId(userId);

            // then
            verify(fcmTokenService).deleteAllByUserId(userId);
        }
    }
}
