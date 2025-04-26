package kr.co.yournews.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.co.yournews.common.BaseTimeEntity;
import kr.co.yournews.domain.notification.converter.StringListConverter;
import kr.co.yournews.domain.notification.type.NotificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
@Entity(name = "notification")
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "news_name")
    private String newsName;

    @Column(name = "post_title", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> postTitle;

    @Column(name = "post_url", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> postUrl;

    @ColumnDefault("false")
    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "public_id")
    private String publicId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder
    public Notification(String newsName, List<String> postTitle, List<String> postUrl,
                        boolean isRead, String publicId, NotificationType type, Long userId) {
        this.newsName = newsName;
        this.postTitle = postTitle;
        this.postUrl = postUrl;
        this.isRead = isRead;
        this.publicId = publicId;
        this.type = type;
        this.userId = userId;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public boolean isReceiver(Long userId) {
        return this.userId != null && this.userId.equals(userId);
    }
}
