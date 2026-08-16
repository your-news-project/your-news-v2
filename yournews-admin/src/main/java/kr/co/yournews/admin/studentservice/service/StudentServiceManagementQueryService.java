package kr.co.yournews.admin.studentservice.service;

import kr.co.yournews.admin.studentservice.dto.StudentServiceManagementDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.exception.StudentServiceErrorType;
import kr.co.yournews.domain.studentservice.service.StudentServiceService;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentServiceManagementQueryService {
    private final StudentServiceService studentServiceService;

    @Transactional(readOnly = true)
    public Page<StudentServiceManagementDto.Summary> getStudentServices(
            StudentServiceStatus status,
            Pageable pageable
    ) {
        Page<StudentService> studentServices = (status == null)
                ? studentServiceService.readAll(pageable)
                : studentServiceService.readByStatus(status, pageable);

        return studentServices.map(StudentServiceManagementDto.Summary::from);
    }

    @Transactional(readOnly = true)
    public StudentServiceManagementDto.Details getStudentServiceById(Long studentServiceId) {
        StudentService studentService = studentServiceService.readById(studentServiceId)
                .orElseThrow(() -> new CustomException(StudentServiceErrorType.NOT_FOUND));

        return StudentServiceManagementDto.Details.from(studentService);
    }
}
