package kr.co.yournews.domain.calendar.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.co.yournews.domain.calendar.type.CalendarType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "calendar")
public class Calendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "article_no")
    private Long articleNo;

    @Column(name = "start_at")
    private LocalDate startAt;

    @Column(name = "end_at")
    private LocalDate endAt;

    @Enumerated(EnumType.STRING)
    private CalendarType type;

    @Builder
    public Calendar(
            String title, Long articleNo,
            LocalDate startAt, LocalDate endAt,
            CalendarType type
    ) {
        this.title = title;
        this.articleNo = articleNo;
        this.startAt = startAt;
        this.endAt = endAt;
        this.type = type;
    }
}
