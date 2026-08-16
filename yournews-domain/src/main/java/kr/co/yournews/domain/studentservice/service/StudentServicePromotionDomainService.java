package kr.co.yournews.domain.studentservice.service;

import kr.co.yournews.domain.studentservice.entity.StudentServicePromotion;
import kr.co.yournews.domain.studentservice.repository.StudentServicePromotionRepository;
import kr.co.yournews.domain.studentservice.type.StudentServicePromotionStatus;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServicePromotionDomainService {
    private static final List<StudentServicePromotionStatus> OPEN_STATUSES = List.of(
            StudentServicePromotionStatus.QUEUED,
            StudentServicePromotionStatus.ACTIVE
    );

    private final StudentServicePromotionRepository repository;

    public StudentServicePromotion save(StudentServicePromotion promotion) {
        return repository.save(promotion);
    }

    public List<StudentServicePromotion> lockAllOpen() {
        return repository.findAllOpenForUpdate(OPEN_STATUSES);
    }

    public List<StudentServicePromotion> readTop5Active(LocalDateTime now) {
        return repository.findTop5Active(
                StudentServicePromotionStatus.ACTIVE,
                StudentServiceStatus.APPROVED,
                now,
                PageRequest.of(0, 5)
        );
    }

    public List<StudentServicePromotion> readOpenByStudentServiceIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return repository.findAllByStudentService_IdInAndStatusIn(ids, OPEN_STATUSES);
    }
}
