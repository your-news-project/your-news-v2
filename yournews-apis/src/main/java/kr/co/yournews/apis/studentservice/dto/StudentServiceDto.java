package kr.co.yournews.apis.studentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.entity.StudentServicePromotion;
import kr.co.yournews.domain.studentservice.type.StudentServiceContentType;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import kr.co.yournews.domain.studentservice.type.StudentServicePromotionStatus;
import kr.co.yournews.domain.user.entity.User;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.List;

public class StudentServiceDto {

    public record Request(
            @NotBlank(message = "제목은 필수 입력입니다.")
            @Size(max = 50, message = "제목은 최대 50자까지 작성할 수 있습니다.")
            String name,

            @NotBlank(message = "소개글은 필수 입력입니다.")
            @Size(max = 1000, message = "소개글은 최대 1000자까지 작성할 수 있습니다.")
            String description,

            @Size(max = 5, message = "홍보 링크는 최대 5개까지 입력할 수 있습니다.")
            List<@Size(max = 500, message = "홍보 링크는 최대 500자까지 입력할 수 있습니다.")
                    @URL(protocol = "https", message = "홍보 링크는 올바른 HTTPS 주소여야 합니다.") String> serviceUrls,

            @NotNull(message = "게시물 유형은 필수 입력입니다.")
            StudentServiceContentType contentType
    ) {
        public Request {
            serviceUrls = normalizeUrls(serviceUrls);
            contentType = contentType == null ? StudentServiceContentType.SERVICE : contentType;
        }

        public StudentService toEntity(User user, List<String> imageUrls) {
            return StudentService.builder()
                    .name(name)
                    .description(description)
                    .serviceUrls(serviceUrls)
                    .imageUrls(imageUrls)
                    .contentType(contentType)
                    .user(user)
                    .build();
        }
    }

    public record UpdateRequest(
            @NotBlank(message = "제목은 필수 입력입니다.")
            @Size(max = 50, message = "제목은 최대 50자까지 작성할 수 있습니다.")
            String name,

            @NotBlank(message = "소개글은 필수 입력입니다.")
            @Size(max = 1000, message = "소개글은 최대 1000자까지 작성할 수 있습니다.")
            String description,

            @Size(max = 5, message = "홍보 링크는 최대 5개까지 입력할 수 있습니다.")
            List<@Size(max = 500, message = "홍보 링크는 최대 500자까지 입력할 수 있습니다.")
                    @URL(protocol = "https", message = "홍보 링크는 올바른 HTTPS 주소여야 합니다.") String> serviceUrls,

            @NotNull(message = "게시물 유형은 필수 입력입니다.")
            StudentServiceContentType contentType,

            @Size(max = 3, message = "기존 이미지는 최대 3장까지 유지할 수 있습니다.")
            List<@URL(protocol = "https", message = "올바른 이미지 URL이어야 합니다.") String> retainedImageUrls
    ) {
        public UpdateRequest {
            serviceUrls = normalizeUrls(serviceUrls);
            contentType = contentType == null ? StudentServiceContentType.SERVICE : contentType;
        }
    }

    public record Response(
            Long id
    ) {
        public static Response of(Long id) {
            return new Response(id);
        }
    }

    public record Summary(
            Long id,
            String name,
            String description,
            List<String> serviceUrls,
            StudentServiceContentType contentType,
            StudentServiceStatus status,
            List<String> imageUrls,
            int clickCount,
            int viewCount,
            int likeCount,
            StudentServicePromotionStatus promotionStatus,
            int promotionRewardCount,
            LocalDateTime promotionEndsAt,
            LocalDateTime createdAt
    ) {
        public static Summary from(StudentService studentService) {
            return from(studentService, null);
        }

        public static Summary from(
                StudentService studentService,
                StudentServicePromotion promotion
        ) {
            return new Summary(
                    studentService.getId(),
                    studentService.getName(),
                    studentService.getDescription(),
                    studentService.getServiceUrls(),
                    studentService.getContentType(),
                    studentService.getStatus(),
                    studentService.getImageUrls(),
                    studentService.getClickCount(),
                    studentService.getViewCount(),
                    studentService.getLikeCount(),
                    promotion == null ? null : promotion.getStatus(),
                    promotion == null ? 0 : promotion.getRewardCount(),
                    promotion == null ? null : promotion.getEndsAt(),
                    studentService.getCreatedAt()
            );
        }
    }

    public record Details(
            Long id,
            String name,
            String description,
            List<String> serviceUrls,
            StudentServiceContentType contentType,
            StudentServiceStatus status,
            List<String> imageUrls,
            int reportCount,
            int clickCount,
            int viewCount,
            int likeCount,
            boolean liked,
            boolean authoredByRequester,
            StudentServicePromotionStatus promotionStatus,
            int promotionRewardCount,
            LocalDateTime promotionEndsAt,
            LocalDateTime createdAt
    ) {
        public static Details of(
                StudentService studentService,
                boolean liked,
                boolean authoredByRequester,
                StudentServicePromotion promotion
        ) {
            return new Details(
                    studentService.getId(),
                    studentService.getName(),
                    studentService.getDescription(),
                    studentService.getServiceUrls(),
                    studentService.getContentType(),
                    studentService.getStatus(),
                    studentService.getImageUrls(),
                    studentService.getReportCount(),
                    studentService.getClickCount(),
                    studentService.getViewCount(),
                    studentService.getLikeCount(),
                    liked,
                    authoredByRequester,
                    promotion == null ? null : promotion.getStatus(),
                    promotion == null ? 0 : promotion.getRewardCount(),
                    promotion == null ? null : promotion.getEndsAt(),
                    studentService.getCreatedAt()
            );
        }
    }

    public record Promotion(
            Long id,
            String name,
            String description,
            List<String> serviceUrls,
            StudentServiceContentType contentType,
            List<String> imageUrls,
            LocalDateTime promotionEndsAt
    ) {
        public static Promotion from(StudentServicePromotion promotion) {
            StudentService studentService = promotion.getStudentService();
            return new Promotion(
                    studentService.getId(),
                    studentService.getName(),
                    studentService.getDescription(),
                    studentService.getServiceUrls(),
                    studentService.getContentType(),
                    studentService.getImageUrls(),
                    promotion.getEndsAt()
            );
        }
    }

    public record PromotionResult(
            StudentServicePromotionStatus status,
            int rewardCount,
            int remainingRewardCount,
            Integer queuePosition,
            LocalDateTime promotionEndsAt
    ) {
    }

    private static List<String> normalizeUrls(List<String> urls) {
        if (urls != null && !urls.isEmpty()) {
            return urls.stream().filter(url -> url != null && !url.isBlank()).distinct().toList();
        }
        return List.of();
    }
}
