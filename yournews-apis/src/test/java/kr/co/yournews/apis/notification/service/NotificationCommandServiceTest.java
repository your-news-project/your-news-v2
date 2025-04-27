package kr.co.yournews.apis.notification.service;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.exception.NotificationErrorType;
import kr.co.yournews.domain.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
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
public class NotificationCommandServiceTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationCommandService notificationCommandService;

    @Test
    @DisplayName("알림 생성 테스트")
    void createNotificationsSuccess() {
        // given
        List<Notification> notifications = List.of(
                Notification.builder().newsName("소식1").postTitle(List.of("제목1")).postUrl(List.of("url1")).build(),
                Notification.builder().newsName("소식2").postTitle(List.of("제목2")).postUrl(List.of("url2")).build(),
                Notification.builder().newsName("소식3").postTitle(List.of("제목3")).postUrl(List.of("url3")).build()
        );

        // when
        notificationCommandService.createNotifications(notifications);

        // then
        verify(notificationService, times(1)).saveAll(notifications);
    }

    @Nested
    @DisplayName("알림 삭제 테스트")
    class DeleteNotificationTest {

        private final Long userId = 1L;
        private final Long notificationId = 1L;

        @Test
        @DisplayName("성공")
        void deleteNotificationSuccess() {
            // given

            Notification notification = mock(Notification.class);

            given(notificationService.readById(notificationId)).willReturn(Optional.of(notification));
            given(notification.isReceiver(userId)).willReturn(true);

            // when
            notificationCommandService.deleteNotification(userId, notificationId);

            // then
            verify(notificationService, times(1)).deleteById(notificationId);
        }

        @Test
        @DisplayName("알림 삭제 실패 - 알림이 존재하지 않음")
        void deleteNotification_fail_notFound() {
            // given
            given(notificationService.readById(notificationId)).willReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> notificationCommandService.deleteNotification(userId, notificationId));

            // then
            assertEquals(NotificationErrorType.NOT_FOUND, exception.getErrorType());
            verify(notificationService, times(1)).readById(notificationId);
            verify(notificationService, never()).deleteById(notificationId);
        }

        @Test
        @DisplayName("알림 삭제 실패 - 수신자 아님")
        void deleteNotification_fail_forbidden() {
            // given
            Notification notification = mock(Notification.class);

            given(notificationService.readById(notificationId)).willReturn(Optional.of(notification));
            given(notification.isReceiver(userId)).willReturn(false);

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> notificationCommandService.deleteNotification(userId, notificationId));

            // then
            assertEquals(NotificationErrorType.FORBIDDEN, exception.getErrorType());
            verify(notificationService, times(1)).readById(notificationId);
            verify(notificationService, never()).deleteById(notificationId);
        }
    }

    @Test
    @DisplayName("오래된 알림 삭제 성공")
    void deleteOldNotification() {
        // given

        // when
        notificationCommandService.deleteOldNotification();

        // then
        verify(notificationService, times(1)).deleteByDateTime(any(LocalDate.class));
    }
}
