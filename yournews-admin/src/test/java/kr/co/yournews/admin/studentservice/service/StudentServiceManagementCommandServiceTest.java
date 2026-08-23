package kr.co.yournews.admin.studentservice.service;

import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.service.StudentServiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentServiceManagementCommandServiceTest {
    @Mock
    private StudentServiceService studentServiceService;

    @Mock
    private StudentServiceStatusNotificationService statusNotificationService;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentServiceManagementCommandService commandService;

    @Test
    void approveChangesStatusAndEnqueuesNotification() {
        given(studentServiceService.readById(1L)).willReturn(Optional.of(studentService));

        commandService.approveStudentService(1L);

        verify(studentService).approve();
        verify(statusNotificationService).enqueueApproved(studentService);
    }

    @Test
    void rejectChangesStatusAndEnqueuesNotification() {
        given(studentServiceService.readById(1L)).willReturn(Optional.of(studentService));

        commandService.rejectStudentService(1L, "링크에 접속할 수 없습니다.");

        verify(studentService).reject("링크에 접속할 수 없습니다.");
        verify(statusNotificationService).enqueueRejected(studentService);
    }

    @Test
    void hideDoesNotEnqueueApprovalNotification() {
        given(studentServiceService.readById(1L)).willReturn(Optional.of(studentService));

        commandService.hideStudentService(1L);

        verify(studentService).hide();
        verify(statusNotificationService, never()).enqueueApproved(studentService);
        verify(statusNotificationService, never()).enqueueRejected(studentService);
    }
}
