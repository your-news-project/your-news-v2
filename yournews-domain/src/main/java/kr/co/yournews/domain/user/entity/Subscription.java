package kr.co.yournews.domain.user.entity;

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
import kr.co.yournews.domain.user.type.SubscriptionPlatform;
import kr.co.yournews.domain.user.type.SubscriptionStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "subscription")
public class Subscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subscriptionId;

    private String productId;

    private LocalDateTime purchaseAt;

    private LocalDateTime expireAt;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlatform platform;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Subscription(
            String subscriptionId, String productId, LocalDateTime purchaseAt,
            LocalDateTime expireAt, SubscriptionPlatform platform,
            SubscriptionStatus status, User user
    ) {
        this.subscriptionId = subscriptionId;
        this.productId = productId;
        this.purchaseAt = purchaseAt;
        this.expireAt = expireAt;
        this.platform = platform;
        this.status = status;
        this.user = user;
    }

    public void updateDatesAndStatus(
            LocalDateTime expireAt,
            SubscriptionStatus status
    ) {
        this.expireAt = expireAt;
        this.status = status;
    }

    public void updateStatus(SubscriptionStatus status) {
        this.status = status;
    }
}
