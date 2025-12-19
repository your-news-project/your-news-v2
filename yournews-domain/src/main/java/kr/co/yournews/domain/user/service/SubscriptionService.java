package kr.co.yournews.domain.user.service;

import kr.co.yournews.domain.user.entity.Subscription;
import kr.co.yournews.domain.user.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public void save(Subscription subscription) {
        subscriptionRepository.save(subscription);
    }

    public Optional<Subscription> readTopByUserIdOrderByCreatedAtDesc(Long userId) {
        return subscriptionRepository.findTopByUser_IdOrderByCreatedAtDesc(userId);
    }

    public Optional<Subscription> readTopBySubscriptionIdOrderByCreatedAtDesc(String subscriptionId) {
        return subscriptionRepository.findTopBySubscriptionIdOrderByCreatedAtDesc(subscriptionId);
    }
}
