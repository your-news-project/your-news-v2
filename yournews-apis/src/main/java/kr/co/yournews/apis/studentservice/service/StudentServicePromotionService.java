package kr.co.yournews.apis.studentservice.service;

import kr.co.yournews.apis.studentservice.dto.StudentServiceDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.entity.StudentServicePromotion;
import kr.co.yournews.domain.studentservice.exception.StudentServiceErrorType;
import kr.co.yournews.domain.studentservice.service.StudentServiceService;
import kr.co.yournews.domain.studentservice.service.StudentServicePromotionDomainService;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import kr.co.yournews.domain.studentservice.type.StudentServicePromotionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServicePromotionService {
    private static final int MAX_ACTIVE_PROMOTIONS = 5;

    private final StudentServiceService studentServiceService;
    private final StudentServicePromotionDomainService promotionDomainService;

    @Transactional
    public StudentServiceDto.PromotionResult addReward(Long userId, Long studentServiceId) {
        LocalDateTime now = LocalDateTime.now();
        List<StudentService> approvedServices = studentServiceService.lockAllApprovedForPromotion();
        List<StudentServicePromotion> promotions = new java.util.ArrayList<>(
                promotionDomainService.lockAllOpen()
        );
        reconcilePromotions(promotions, now);

        StudentService target = approvedServices.stream()
                .filter(service -> service.getId().equals(studentServiceId))
                .findFirst()
                .orElseThrow(() -> new CustomException(StudentServiceErrorType.NOT_PROMOTABLE));

        if (!target.isAuthor(userId)) {
            throw new CustomException(StudentServiceErrorType.FORBIDDEN);
        }
        StudentServicePromotion promotion = promotions.stream()
                .filter(item -> item.getStudentService().getId().equals(studentServiceId))
                .filter(item -> item.getStatus() != StudentServicePromotionStatus.COMPLETED)
                .findFirst()
                .orElse(null);

        if (promotion == null) {
            promotion = promotionDomainService.save(new StudentServicePromotion(target, now));
            promotions.add(promotion);
        } else if (!promotion.addReward()) {
            throw new CustomException(StudentServiceErrorType.PROMOTION_LIMIT_REACHED);
        }

        reconcilePromotions(promotions, now);
        StudentServicePromotionStatus status = promotion.getStatus();
        Integer queuePosition = status == StudentServicePromotionStatus.QUEUED
                ? queuedPromotions(promotions).indexOf(promotion) + 1
                : null;

        return new StudentServiceDto.PromotionResult(
                status,
                promotion.getRewardCount(),
                StudentServicePromotion.MAX_REWARD_COUNT - promotion.getRewardCount(),
                queuePosition,
                promotion.getEndsAt()
        );
    }

    @Transactional
    public List<StudentServiceDto.Promotion> getActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        studentServiceService.lockAllApprovedForPromotion();
        List<StudentServicePromotion> promotions = promotionDomainService.lockAllOpen();
        reconcilePromotions(promotions, now);

        return promotionDomainService.readTop5Active(now).stream()
                .map(StudentServiceDto.Promotion::from)
                .toList();
    }

    private void reconcilePromotions(List<StudentServicePromotion> promotions, LocalDateTime now) {
        promotions.forEach(promotion -> {
            if (promotion.getStudentService().getStatus() != StudentServiceStatus.APPROVED) {
                promotion.complete();
            } else {
                promotion.completeIfExpired(now);
            }
        });
        long activeCount = promotions.stream()
                .filter(promotion -> promotion.isActive(now))
                .count();

        int availableSlots = Math.max(0, MAX_ACTIVE_PROMOTIONS - (int) activeCount);
        queuedPromotions(promotions).stream()
                .limit(availableSlots)
                .forEach(promotion -> promotion.activate(now));
    }

    private List<StudentServicePromotion> queuedPromotions(List<StudentServicePromotion> promotions) {
        return promotions.stream()
                .filter(StudentServicePromotion::isQueued)
                .sorted(Comparator.comparing(StudentServicePromotion::getQueuedAt)
                        .thenComparing(promotion -> promotion.getId() == null ? Long.MAX_VALUE : promotion.getId()))
                .toList();
    }
}
