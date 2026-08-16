package kr.co.yournews.apis.studentservice.service;

import kr.co.yournews.apis.studentservice.dto.StudentServiceDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.entity.StudentServicePromotion;
import kr.co.yournews.domain.studentservice.exception.StudentServiceErrorType;
import kr.co.yournews.domain.studentservice.service.StudentServicePromotionDomainService;
import kr.co.yournews.domain.studentservice.service.StudentServiceService;
import kr.co.yournews.domain.studentservice.type.StudentServicePromotionStatus;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import kr.co.yournews.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class StudentServicePromotionServiceTest {

    @Mock
    private StudentServiceService studentServiceService;

    @Mock
    private StudentServicePromotionDomainService promotionDomainService;

    @InjectMocks
    private StudentServicePromotionService promotionService;

    @BeforeEach
    void setUp() {
        lenient().when(promotionDomainService.save(any(StudentServicePromotion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void activatesFirstPromotionForThreeHours() {
        StudentService target = approvedService(10L, 1L);
        when(studentServiceService.lockAllApprovedForPromotion()).thenReturn(List.of(target));
        when(promotionDomainService.lockAllOpen()).thenReturn(List.of());

        LocalDateTime before = LocalDateTime.now();
        StudentServiceDto.PromotionResult result = promotionService.addReward(1L, 10L);

        assertThat(result.status()).isEqualTo(StudentServicePromotionStatus.ACTIVE);
        assertThat(result.rewardCount()).isEqualTo(1);
        assertThat(result.remainingRewardCount()).isEqualTo(7);
        assertThat(result.queuePosition()).isNull();
        assertThat(result.promotionEndsAt()).isAfterOrEqualTo(before.plusHours(3));
    }

    @Test
    void queuesSixthPromotionBehindFiveActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        List<StudentServicePromotion> promotions = new ArrayList<>();
        List<StudentService> approvedServices = new ArrayList<>();
        for (long id = 1; id <= 5; id++) {
            StudentService activeService = approvedService(id, id);
            StudentServicePromotion active = new StudentServicePromotion(
                    activeService,
                    now.minusMinutes(1)
            );
            active.activate(now.minusMinutes(1));
            approvedServices.add(activeService);
            promotions.add(active);
        }
        StudentService target = approvedService(10L, 100L);
        approvedServices.add(target);
        when(studentServiceService.lockAllApprovedForPromotion()).thenReturn(approvedServices);
        when(promotionDomainService.lockAllOpen()).thenReturn(promotions);

        StudentServiceDto.PromotionResult result = promotionService.addReward(100L, 10L);

        assertThat(result.status()).isEqualTo(StudentServicePromotionStatus.QUEUED);
        assertThat(result.queuePosition()).isEqualTo(1);
        assertThat(result.promotionEndsAt()).isNull();
    }

    @Test
    void rejectsMoreThanEightRewardsInOneCampaign() {
        StudentService target = approvedService(10L, 1L);
        StudentServicePromotion promotion = new StudentServicePromotion(target, LocalDateTime.now());
        for (int count = 1; count < StudentServicePromotion.MAX_REWARD_COUNT; count++) {
            promotion.addReward();
        }
        when(studentServiceService.lockAllApprovedForPromotion()).thenReturn(List.of(target));
        when(promotionDomainService.lockAllOpen()).thenReturn(List.of(promotion));

        assertThatThrownBy(() -> promotionService.addReward(1L, 10L))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                        assertThat(exception.getErrorType())
                                .isEqualTo(StudentServiceErrorType.PROMOTION_LIMIT_REACHED));
    }

    @Test
    void createsNewCampaignAfterPreviousTwentyFourHoursExpired() {
        StudentService target = approvedService(10L, 1L);
        LocalDateTime previousStart = LocalDateTime.now().minusHours(25);
        StudentServicePromotion previous = new StudentServicePromotion(target, previousStart);
        for (int count = 1; count < StudentServicePromotion.MAX_REWARD_COUNT; count++) {
            previous.addReward();
        }
        previous.activate(previousStart);
        when(studentServiceService.lockAllApprovedForPromotion()).thenReturn(List.of(target));
        when(promotionDomainService.lockAllOpen()).thenReturn(List.of(previous));

        StudentServiceDto.PromotionResult result = promotionService.addReward(1L, 10L);

        assertThat(previous.getStatus()).isEqualTo(StudentServicePromotionStatus.COMPLETED);
        assertThat(result.status()).isEqualTo(StudentServicePromotionStatus.ACTIVE);
        assertThat(result.rewardCount()).isEqualTo(1);
        assertThat(result.remainingRewardCount()).isEqualTo(7);
    }

    @Test
    void activatesOldestQueuedCampaignWhenAnActiveSlotExpires() {
        LocalDateTime now = LocalDateTime.now();
        List<StudentService> approvedServices = new ArrayList<>();
        List<StudentServicePromotion> openPromotions = new ArrayList<>();

        for (long id = 1; id <= 4; id++) {
            StudentService service = approvedService(id, id);
            StudentServicePromotion promotion = new StudentServicePromotion(
                    service,
                    now.minusHours(1)
            );
            promotion.activate(now.minusHours(1));
            approvedServices.add(service);
            openPromotions.add(promotion);
        }

        StudentService expiredService = approvedService(5L, 5L);
        StudentServicePromotion expired = new StudentServicePromotion(
                expiredService,
                now.minusHours(4)
        );
        expired.activate(now.minusHours(4));
        approvedServices.add(expiredService);
        openPromotions.add(expired);

        StudentService oldestQueuedService = approvedService(6L, 6L);
        StudentServicePromotion oldestQueued = new StudentServicePromotion(
                oldestQueuedService,
                now.minusMinutes(30)
        );
        approvedServices.add(oldestQueuedService);
        openPromotions.add(oldestQueued);

        StudentService newTarget = approvedService(7L, 7L);
        approvedServices.add(newTarget);
        when(studentServiceService.lockAllApprovedForPromotion()).thenReturn(approvedServices);
        when(promotionDomainService.lockAllOpen()).thenReturn(openPromotions);

        StudentServiceDto.PromotionResult result = promotionService.addReward(7L, 7L);

        assertThat(expired.getStatus()).isEqualTo(StudentServicePromotionStatus.COMPLETED);
        assertThat(oldestQueued.getStatus()).isEqualTo(StudentServicePromotionStatus.ACTIVE);
        assertThat(result.status()).isEqualTo(StudentServicePromotionStatus.QUEUED);
        assertThat(result.queuePosition()).isEqualTo(1);
    }

    private StudentService approvedService(Long id, Long userId) {
        StudentService service = StudentService.builder()
                .name("홍보 " + id)
                .description("소개")
                .imageUrls(List.of())
                .user(User.builder().build())
                .build();
        ReflectionTestUtils.setField(service, "id", id);
        ReflectionTestUtils.setField(service, "userId", userId);
        ReflectionTestUtils.setField(service, "status", StudentServiceStatus.APPROVED);
        return service;
    }
}
