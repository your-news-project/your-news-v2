package kr.co.yournews.admin.studentservice.dto;

import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import kr.co.yournews.domain.studentservice.type.StudentServiceContentType;

import java.time.LocalDateTime;
import java.util.List;

public class StudentServiceManagementDto {

    public record Summary(
            Long id,
            Long userId,
            String name,
            String description,
            List<String> serviceUrls,
            StudentServiceContentType contentType,
            StudentServiceStatus status,
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
