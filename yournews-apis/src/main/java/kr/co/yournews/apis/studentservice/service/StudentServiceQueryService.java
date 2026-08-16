package kr.co.yournews.apis.studentservice.service;

import kr.co.yournews.apis.studentservice.dto.StudentServiceDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.entity.StudentServicePromotion;
import kr.co.yournews.domain.studentservice.exception.StudentServiceErrorType;
import kr.co.yournews.domain.studentservice.service.StudentServiceLikeService;
import kr.co.yournews.domain.studentservice.service.StudentServiceService;
import kr.co.yournews.domain.studentservice.service.StudentServicePromotionDomainService;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import kr.co.yournews.domain.studentservice.type.StudentServiceContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class StudentServiceQueryService {
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private final StudentServiceService studentServiceService;
    private final StudentServiceLikeService studentServiceLikeService;
    private final StudentServicePromotionDomainService promotionDomainService;

    @Transactional(readOnly = true)
    public Page<StudentServiceDto.Summary> getStudentServices(
            StudentServiceContentType contentType,
            Pageable pageable
    ) {
        Page<StudentService> services = contentType == null
                ? studentServiceService.readByStatus(StudentServiceStatus.APPROVED, pageable)
                : studentServiceService.readByStatusAndContentType(
                        StudentServiceStatus.APPROVED,
                        contentType,
                        pageable
                );
        return services
                .map(StudentServiceDto.Summary::from);
    }

    @Transactional(readOnly = true)
    public Page<StudentServiceDto.Summary> getMyStudentServices(Long userId, Pageable pageable) {
        Page<StudentService> services = studentServiceService.readByUserId(userId, pageable);
        Map<Long, StudentServicePromotion> promotions = currentPromotions(
                services.getContent().stream().map(StudentService::getId).toList()
        );
        return services.map(service -> StudentServiceDto.Summary.from(
                service,
                promotions.get(service.getId())
        ));
    }

    @Transactional(readOnly = true)
    public List<StudentServiceDto.Summary> getPopularStudentServices() {
        LocalDate today = LocalDate.now(KOREA_ZONE_ID);
        return studentServiceService.readTop3PopularSince(today.minusDays(13)).stream()
                .map(StudentServiceDto.Summary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentServiceDto.Summary> getLatestStudentServices() {
        LocalDateTime startOfWeek = LocalDate.now(KOREA_ZONE_ID)
                .minusDays(6)
                .atStartOfDay();
        return studentServiceService.readTop3LatestSince(
                        StudentServiceStatus.APPROVED,
                        startOfWeek
                ).stream()
                .map(StudentServiceDto.Summary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentServiceDto.Details getStudentServiceById(Long studentServiceId, Long userId) {
        StudentService studentService = studentServiceService.readById(studentServiceId)
                .orElseThrow(() -> new CustomException(StudentServiceErrorType.NOT_FOUND));

        boolean publiclyVisible = studentService.getStatus() == StudentServiceStatus.APPROVED;
        boolean authoredByRequester = userId != null && studentService.isAuthor(userId);

        if (!publiclyVisible && !authoredByRequester) {
            throw new CustomException(StudentServiceErrorType.NOT_FOUND);
        }

        boolean liked = userId != null
                && studentServiceLikeService.existsByUserIdAndStudentServiceId(userId, studentServiceId);

        StudentServicePromotion promotion = currentPromotions(List.of(studentServiceId))
                .get(studentServiceId);

        return StudentServiceDto.Details.of(
                studentService,
                liked,
                authoredByRequester,
                promotion
        );
    }

    private Map<Long, StudentServicePromotion> currentPromotions(List<Long> studentServiceIds) {
        LocalDateTime now = LocalDateTime.now();
        return promotionDomainService.readOpenByStudentServiceIds(studentServiceIds).stream()
                .filter(promotion -> promotion.isQueued() || promotion.isActive(now))
                .collect(Collectors.toMap(
                        StudentServicePromotion::getStudentServiceId,
                        Function.identity(),
                        (first, ignored) -> first
                ));
    }
}
