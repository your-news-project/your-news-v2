package kr.co.yournews.admin.studentservice.controller;

import kr.co.yournews.admin.studentservice.service.StudentServiceManagementCommandService;
import kr.co.yournews.admin.studentservice.service.StudentServiceManagementQueryService;
import kr.co.yournews.common.response.success.SuccessResponse;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/student-services")
@RequiredArgsConstructor
public class StudentServiceManagementController {
    private final StudentServiceManagementCommandService studentServiceManagementCommandService;
    private final StudentServiceManagementQueryService studentServiceManagementQueryService;

    @GetMapping
    public ResponseEntity<?> getStudentServices(
            @RequestParam(required = false) StudentServiceStatus status,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        studentServiceManagementQueryService.getStudentServices(status, pageable)
                )
        );
    }

    @GetMapping("/{studentServiceId}")
    public ResponseEntity<?> getStudentServiceById(@PathVariable Long studentServiceId) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        studentServiceManagementQueryService.getStudentServiceById(studentServiceId)
                )
        );
    }

    @PatchMapping("/{studentServiceId}/approve")
    public ResponseEntity<?> approveStudentService(@PathVariable Long studentServiceId) {
        studentServiceManagementCommandService.approveStudentService(studentServiceId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PatchMapping("/{studentServiceId}/reject")
    public ResponseEntity<?> rejectStudentService(@PathVariable Long studentServiceId) {
        studentServiceManagementCommandService.rejectStudentService(studentServiceId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @PatchMapping("/{studentServiceId}/hide")
    public ResponseEntity<?> hideStudentService(@PathVariable Long studentServiceId) {
        studentServiceManagementCommandService.hideStudentService(studentServiceId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
