package kr.co.yournews.domain.studentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "student_service_daily_stat")
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_student_service_daily_stat_service_date",
                columnNames = {"student_service_id", "stat_date"}
        )
})
public class StudentServiceDailyStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_service_id", nullable = false)
    private Long studentServiceId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "click_count", nullable = false)
    private int clickCount;

    @Column(name = "like_count", nullable = false)
    private int likeCount;
}
