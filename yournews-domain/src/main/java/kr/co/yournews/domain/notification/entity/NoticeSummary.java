package kr.co.yournews.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.co.yournews.common.BaseTimeEntity;
import kr.co.yournews.domain.notification.type.SummaryStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Entity(name = "notice_summary")
public class NoticeSummary extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    // URL 해시값 (조회/중복 방지용)
    @Column(name = "url_hash", nullable = false)
    private String urlHash;

    // 공지 요약
    @Column(columnDefinition = "TEXT")
    private String summary;

    // 요약 성공 여부
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SummaryStatus status;

    @Builder
    public NoticeSummary(String url, String urlHash, String summary, SummaryStatus status) {
        this.url = url;
        this.urlHash = urlHash;
        this.summary = summary;
        this.status = status;
    }

    public void success(String summary) {
        this.summary = summary;
        this.status = SummaryStatus.READY;
    }
}
