package kr.co.yournews.domain.studentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kr.co.yournews.common.BaseTimeEntity;
import kr.co.yournews.domain.studentservice.converter.StudentServiceImageUrlConverter;
import kr.co.yournews.domain.studentservice.converter.StudentServiceUrlConverter;
import kr.co.yournews.domain.studentservice.type.StudentServiceContentType;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import kr.co.yournews.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "student_service")
public class StudentService extends BaseTimeEntity {
    private static final int AUTO_HIDE_REPORT_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "service_url", length = 500)
    private String legacyServiceUrl;

    @Convert(converter = StudentServiceUrlConverter.class)
    @Column(name = "service_urls", columnDefinition = "TEXT")
    private List<String> serviceUrls;

    @Convert(converter = StudentServiceImageUrlConverter.class)
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private List<String> imageUrls;

    @Column(nullable = false)
    private String platform;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type")
    private StudentServiceContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentServiceStatus status;

    @Column(name = "report_count", nullable = false)
    private int reportCount;

    @Column(name = "click_count", nullable = false)
    private int clickCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Builder
    public StudentService(String name, String description,
                          List<String> serviceUrls, List<String> imageUrls,
                          StudentServiceContentType contentType, User user) {
        this.name = name;
        this.description = description;
        this.serviceUrls = normalizeUrls(serviceUrls);
        this.legacyServiceUrl = this.serviceUrls.isEmpty() ? null : this.serviceUrls.get(0);
        this.imageUrls = imageUrls == null ? List.of() : List.copyOf(imageUrls);
        this.platform = "WEB";
        this.contentType = contentType == null ? StudentServiceContentType.SERVICE : contentType;
        this.status = StudentServiceStatus.PENDING;
        this.reportCount = 0;
        this.clickCount = 0;
        this.viewCount = 0;
        this.likeCount = 0;
        this.user = user;
    }

    public void approve() {
        this.status = StudentServiceStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void update(String name, String description, List<String> serviceUrls,
                       StudentServiceContentType contentType, List<String> imageUrls) {
        this.name = name;
        this.description = description;
        this.serviceUrls = normalizeUrls(serviceUrls);
        this.legacyServiceUrl = this.serviceUrls.isEmpty() ? null : this.serviceUrls.get(0);
        this.contentType = contentType == null ? StudentServiceContentType.SERVICE : contentType;
        this.imageUrls = List.copyOf(imageUrls);
    }

    public List<String> getServiceUrls() {
        if (serviceUrls != null && !serviceUrls.isEmpty()) {
            return List.copyOf(serviceUrls);
        }
        return legacyServiceUrl == null || legacyServiceUrl.isBlank()
                ? List.of()
                : List.of(legacyServiceUrl);
    }

    public StudentServiceContentType getContentType() {
        return contentType == null ? StudentServiceContentType.SERVICE : contentType;
    }

    private static List<String> normalizeUrls(List<String> urls) {
        if (urls != null && !urls.isEmpty()) {
            return urls.stream().filter(url -> url != null && !url.isBlank()).distinct().toList();
        }
        return List.of();
    }

    public void reject() {
        this.status = StudentServiceStatus.REJECTED;
    }

    public void hide() {
        this.status = StudentServiceStatus.HIDDEN;
    }

    public void report() {
        this.reportCount++;

        if (this.reportCount >= AUTO_HIDE_REPORT_COUNT) {
            hide();
        }
    }

    public boolean isAuthor(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    public boolean isPending() {
        return this.status == StudentServiceStatus.PENDING;
    }

}
