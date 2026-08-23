package kr.co.yournews.admin.studentservice.service;

import kr.co.yournews.common.constant.FcmTarget;
import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.service.FcmTokenService;
import kr.co.yournews.infra.notification.NotificationOutboxEnqueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceStatusNotificationService {
    private static final String APPROVED_TITLE = "캠퍼스 홍보 승인 완료";
    private static final String REJECTED_TITLE = "캠퍼스 홍보 반려 안내";

    private final FcmTokenService fcmTokenService;
    private final NotificationOutboxEnqueueService notificationOutboxEnqueueService;

    public void enqueueApproved(StudentService studentService) {
        enqueue(
                studentService,
                APPROVED_TITLE,
                String.format("'%s' 게시물이 승인되어 공개되었습니다.", studentService.getName())
        );
    }

    public void enqueueRejected(StudentService studentService) {
        enqueue(
                studentService,
                REJECTED_TITLE,
                String.format(
                        "'%s' 게시물이 반려되었습니다.\n사유: %s",
                        studentService.getName(),
                        studentService.getRejectionReason()
                )
        );
    }

    private void enqueue(StudentService studentService, String title, String content) {
        List<FcmToken> tokens = fcmTokenService.readAllByUserIds(List.of(studentService.getUserId()));
        notificationOutboxEnqueueService.enqueueMessages(
                tokens,
                title,
                content,
                FcmTarget.STUDENT_SERVICE,
                studentService.getId().toString()
        );
    }
}
