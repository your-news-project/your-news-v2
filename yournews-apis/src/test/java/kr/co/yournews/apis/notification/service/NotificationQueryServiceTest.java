package kr.co.yournews.apis.notification.service;

import kr.co.yournews.apis.notification.dto.NotificationDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationQueryServiceTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationQueryService notificationQueryService;

    @Nested
    @DisplayName("특정 알림 조회 테스트")
    class GetNotificationTest {

        @Test
        @DisplayName("성공")
        void getNotificationByIdSuccess() {
            // given
            Long notificationId = 1L;
            Notification notification = Notification.builder()
                    .newsName("공지사항")
                    .postTitle(List.of("제목"))
                    .postUrl(List.of("url"))
                    .isRead(false)
                    .build();

            given(notificationService.readById(notificationId)).willReturn(Optional.of(notification));

            // when
            NotificationDto.Details result = notificationQueryService.getNotificationById(notificationId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.newsName()).isEqualTo("공지사항");
            assertThat(notification.isRead()).isTrue();
        }

        @Test
        @DisplayName("실패 - 알림이 존재하지 않음")
        void getNotificationByIdFailNotificationNotFound() {
            // given
            Long notificationId = 1L;
            given(notificationService.readById(notificationId)).willReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> notificationQueryService.getNotificationById(notificationId));

            // then
            assertEquals(NotificationErrorType.NOT_FOUND, exception.getErrorType());
            verify(notificationService, times(1)).readById(notificationId);
        }
    }

    @Test
    @DisplayName("사용자 알림 전체 조회")
    void getNotificationsByUserId_success() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Notification mockNotification = Notification.builder()
                .newsName("공지")
                .postTitle(List.of("제목"))
                .postUrl(List.of("url"))
                .isRead(false)
                .build();

        Page<Notification> page = new PageImpl<>(List.of(mockNotification));
        given(notificationService.readAllByUserId(userId, pageable)).willReturn(page);

        // when
        Page<NotificationDto.Summary> result = notificationQueryService.getNotificationsByUserId(userId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).newsName()).isEqualTo("공지");
    }

    @Test
    @DisplayName("사용자 알림 조회 - 읽음 여부 필터")
    void getNotificationsByUserIdAndIsRead_success() {
        // given
        Long userId = 1L;
        boolean isRead = false;
        Pageable pageable = PageRequest.of(0, 10);
        Notification mockNotification = Notification.builder()
                .newsName("테스트")
                .postTitle(List.of("알림제목"))
                .postUrl(List.of("url"))
                .isRead(isRead)
                .build();

        Page<Notification> page = new PageImpl<>(List.of(mockNotification));
        given(notificationService.readAllByUserIdAndIsRead(userId, isRead, pageable)).willReturn(page);

        // when
        Page<NotificationDto.Summary> result = notificationQueryService.getNotificationsByUserIdAndIsRead(userId, isRead, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).newsName()).isEqualTo("테스트");
    }
}
