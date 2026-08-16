package kr.co.yournews.admin.studentservice.service;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.exception.StudentServiceErrorType;
import kr.co.yournews.domain.studentservice.service.StudentServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceManagementCommandService {
    private final StudentServiceService studentServiceService;

    @Transactional
    public void approveStudentService(Long studentServiceId) {
        StudentService studentService = getStudentService(studentServiceId);
        studentService.approve();
        log.info("[ADMIN 학생 서비스 승인 완료] studentServiceId: {}", studentServiceId);
    }

    @Transactional
    public void rejectStudentService(Long studentServiceId) {
        StudentService studentService = getStudentService(studentServiceId);
        studentService.reject();
        log.info("[ADMIN 학생 서비스 거절 완료] studentServiceId: {}", studentServiceId);
    }

    @Transactional
    public void hideStudentService(Long studentServiceId) {
        StudentService studentService = getStudentService(studentServiceId);
        studentService.hide();
        log.info("[ADMIN 학생 서비스 숨김 완료] studentServiceId: {}", studentServiceId);
    }

    private StudentService getStudentService(Long studentServiceId) {
        return studentServiceService.readById(studentServiceId)
                .orElseThrow(() -> new CustomException(StudentServiceErrorType.NOT_FOUND));
    }
}
