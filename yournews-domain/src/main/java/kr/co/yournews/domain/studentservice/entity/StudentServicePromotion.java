package kr.co.yournews.domain.studentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import kr.co.yournews.common.BaseTimeEntity;
import kr.co.yournews.domain.studentservice.type.StudentServicePromotionStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Entity(name = "student_service_promotion")
@Table(
        name = "student_service_promotion",
        indexes = {
                @Index(name = "idx_promotion_status_queued", columnList = "status, queued_at"),
                @Index(name = "idx_promotion_status_ends", columnList = "status, ends_at"),
                @Index(name = "idx_promotion_service_status", columnList = "student_service_id, status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentServicePromotion extends BaseTimeEntity {
    public static final int MAX_REWARD_COUNT = 8;
    private static final int HOURS_PER_REWARD = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_service_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private StudentService studentService;

    @Column(name = "student_service_id", insertable = false, updatable = false)
    private Long studentServiceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentServicePromotionStatus status;

    @Column(name = "reward_count", nullable = false)
    private int rewardCount;

    @Column(name = "queued_at", nullable = false)
    private LocalDateTime queuedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    public StudentServicePromotion(StudentService studentService, LocalDateTime queuedAt) {
        this.studentService = studentService;
        this.status = StudentServicePromotionStatus.QUEUED;
        this.rewardCount = 1;
        this.queuedAt = queuedAt;
    }

    public boolean addReward() {
        if (status == StudentServicePromotionStatus.COMPLETED
                || rewardCount >= MAX_REWARD_COUNT) {
            return false;
        }
        rewardCount++;
        if (status == StudentServicePromotionStatus.ACTIVE) {
            endsAt = endsAt.plusHours(HOURS_PER_REWARD);
        }
        return true;
    }

    public void activate(LocalDateTime now) {
        if (status != StudentServicePromotionStatus.QUEUED) {
            return;
        }
        status = StudentServicePromotionStatus.ACTIVE;
        startedAt = now;
        endsAt = now.plusHours((long) rewardCount * HOURS_PER_REWARD);
    }

    public void completeIfExpired(LocalDateTime now) {
        if (status == StudentServicePromotionStatus.ACTIVE
                && endsAt != null
                && !endsAt.isAfter(now)) {
            complete();
        }
    }

    public void complete() {
        status = StudentServicePromotionStatus.COMPLETED;
    }

    public boolean isActive(LocalDateTime now) {
        return status == StudentServicePromotionStatus.ACTIVE
                && endsAt != null
                && endsAt.isAfter(now);
    }

    public boolean isQueued() {
        return status == StudentServicePromotionStatus.QUEUED;
    }
}
