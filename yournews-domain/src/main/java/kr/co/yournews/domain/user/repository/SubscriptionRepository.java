package kr.co.yournews.domain.user.repository;

import kr.co.yournews.domain.user.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findTopBySubscriptionIdOrderByCreatedAtDesc(String subscriptionId);
    Optional<Subscription> findTopByUser_IdOrderByCreatedAtDesc(Long userId);
}
