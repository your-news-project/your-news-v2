package kr.co.yournews.apis.notification.service;

import kr.co.yournews.apis.notification.dto.NotificationDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.exception.NotificationErrorType;
import kr.co.yournews.domain.notification.service.NoticeSummaryService;
import kr.co.yournews.domain.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    private NoticeSummaryService noticeSummaryService;

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

    @Test
    @DisplayName("모든 알림 읽음 테스트")
    void markAllNotificationsAsReadTest() {
        // given
        Long userId = 1L;

        // when
        notificationCommandService.markAllNotificationsAsRead(userId);

        // then
        verify(notificationService, times(1)).markAllAsRead(userId);
    }


    @Test
    @DisplayName("알림 북마크 설정 테스트")
    void ChangeNotificationBookmarkTest() {
        // given
        Long userId = 1L;
        Long notificationId = 1L;

        Notification notification = Notification.builder()
                .isBookmarked(false)
                .userId(userId)
                .build();

        NotificationDto.BookmarkRequest request =
                new NotificationDto.BookmarkRequest(true);

        given(notificationService.readById(notificationId)).willReturn(Optional.of(notification));

        // when
        notificationCommandService.changeNotificationBookmark(userId, notificationId, request);

        // then
        assertThat(notification.isBookmarked()).isTrue();
    }


    @Nested
    @DisplayName("알림 삭제 테스트")
    class DeleteNotificationTest {

        private final Long userId = 1L;
        private final Long notificationId = 1L;

        @Test
        @DisplayName("성공")
        void success() {
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
    @DisplayName("선택 알림 삭제 테스트")
    void deleteAllByUserIdAndIdInTest() {
        // given
        Long userId = 1L;
        List<Long> notificationIds = List.of(1L, 2L, 3L);
        NotificationDto.DeleteRequest request = new NotificationDto.DeleteRequest(notificationIds);

        // when
        notificationCommandService.deleteAllByUserIdAndIdIn(userId, request);

        // then
        verify(notificationService, times(1)).deleteAllByUserIdAndIdIn(userId, notificationIds);
    }

    @Test
    @DisplayName("오래된 알림 삭제 테스트")
    void deleteOldNotificationTest() {
        // given

        // when
        notificationCommandService.deleteOldNotification();

        // then
        verify(notificationService, times(1)).deleteByDateTime(any(LocalDateTime.class));
        verify(noticeSummaryService, times(1)).deleteByDateTime(any(LocalDateTime.class));
    }
}
