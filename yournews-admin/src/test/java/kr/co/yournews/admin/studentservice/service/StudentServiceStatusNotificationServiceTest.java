package kr.co.yournews.admin.studentservice.service;

import kr.co.yournews.common.constant.FcmTarget;
import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.infra.notification.NotificationOutboxEnqueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentServiceStatusNotificationServiceTest {
    @Mock
    private FcmTokenService fcmTokenService;

    @Mock
    private NotificationOutboxEnqueueService notificationOutboxEnqueueService;

    @Mock
    private StudentService studentService;

    @Mock
    private FcmToken fcmToken;

    @InjectMocks
    private StudentServiceStatusNotificationService notificationService;

    @Test
    void approvedNotificationTargetsStudentServiceDetails() {
        given(studentService.getId()).willReturn(17L);
        given(studentService.getUserId()).willReturn(3L);
        given(studentService.getName()).willReturn("동아리 모집");
        given(fcmTokenService.readAllByUserIds(List.of(3L))).willReturn(List.of(fcmToken));

        notificationService.enqueueApproved(studentService);

        verify(notificationOutboxEnqueueService).enqueueMessages(
                List.of(fcmToken),
                "캠퍼스 홍보 승인 완료",
                "'동아리 모집' 게시물이 승인되어 공개되었습니다.",
                FcmTarget.STUDENT_SERVICE,
                "17"
        );
    }

    @Test
    void rejectedNotificationTargetsStudentServiceDetails() {
        given(studentService.getId()).willReturn(17L);
        given(studentService.getUserId()).willReturn(3L);
        given(studentService.getName()).willReturn("동아리 모집");
        given(studentService.getRejectionReason()).willReturn("등록한 링크에 접속할 수 없습니다.");
        given(fcmTokenService.readAllByUserIds(List.of(3L))).willReturn(List.of(fcmToken));

        notificationService.enqueueRejected(studentService);

        verify(notificationOutboxEnqueueService).enqueueMessages(
                List.of(fcmToken),
                "캠퍼스 홍보 반려 안내",
                "'동아리 모집' 게시물이 반려되었습니다.\n사유: 등록한 링크에 접속할 수 없습니다.",
                FcmTarget.STUDENT_SERVICE,
                "17"
        );
    }
}
