package kr.co.yournews.apis.subscription.service;

import kr.co.yournews.apis.subscription.dto.SubscriptionDto;
import kr.co.yournews.domain.user.entity.Subscription;
import kr.co.yournews.domain.user.service.SubscriptionService;
import kr.co.yournews.domain.user.type.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionQueryService {
    private final SubscriptionService subscriptionService;

    /**
     * 사용자의 현재 구독 상태를 조회하는 메서드.
     * - 없다면 null로 반환.
     *
     * @param userId : 사용자 ID
     * @return : 구독 상태 및 만료일 정보 DTO
     */
    @Transactional(readOnly = true)
    public SubscriptionDto.Response getUserSubscriptionStatus(Long userId) {
        Subscription subscription =
                subscriptionService.readTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);

        SubscriptionStatus status = (subscription != null)
                ? subscription.getStatus()
                : null;

        LocalDateTime expireAt = (subscription != null)
                ? subscription.getExpireAt()
                : null;

        return SubscriptionDto.Response.of(status, expireAt);
    }
}
