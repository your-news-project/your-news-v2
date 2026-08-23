package kr.co.yournews.admin.studentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import kr.co.yournews.domain.studentservice.type.StudentServiceContentType;

import java.time.LocalDateTime;
import java.util.List;

public class StudentServiceManagementDto {

    public record RejectRequest(
            @NotBlank(message = "반려 사유는 필수입니다.")
            @Size(max = 500, message = "반려 사유는 최대 500자까지 작성할 수 있습니다.")
            String rejectionReason
    ) {
    }

    public record Summary(
            Long id,
            Long userId,
            String name,
            String description,
            List<String> serviceUrls,
            StudentServiceContentType contentType,
            StudentServiceStatus status,
            String rejectionReason,
            List<String> imageUrls,
            int reportCount,
            int clickCount,
            int likeCount,
            LocalDateTime createdAt
    ) {
        public static Summary from(StudentService studentService) {
            return new Summary(
                    studentService.getId(),
                    studentService.getUserId(),
                    studentService.getName(),
                    studentService.getDescription(),
                    studentService.getServiceUrls(),
                    studentService.getContentType(),
                    studentService.getStatus(),
                    studentService.getRejectionReason(),
                    studentService.getImageUrls(),
                    studentService.getReportCount(),
                    studentService.getClickCount(),
                    studentService.getLikeCount(),
                    studentService.getCreatedAt()
            );
        }
    }

    public record Details(
            Long id,
            Long userId,
            String name,
            String description,
            List<String> serviceUrls,
            StudentServiceContentType contentType,
            StudentServiceStatus status,
            String rejectionReason,
            List<String> imageUrls,
            int reportCount,
            int clickCount,
            int likeCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static Details from(StudentService studentService) {
            return new Details(
                    studentService.getId(),
                    studentService.getUserId(),
                    studentService.getName(),
                    studentService.getDescription(),
                    studentService.getServiceUrls(),
                    studentService.getContentType(),
                    studentService.getStatus(),
                    studentService.getRejectionReason(),
                    studentService.getImageUrls(),
                    studentService.getReportCount(),
                    studentService.getClickCount(),
                    studentService.getLikeCount(),
                    studentService.getCreatedAt(),
                    studentService.getUpdatedAt()
            );
        }
    }
}
