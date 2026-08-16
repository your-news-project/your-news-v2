package kr.co.yournews.domain.studentservice.service;

import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.repository.StudentServiceRepository;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import kr.co.yournews.domain.studentservice.type.StudentServiceContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StudentServiceService {
    private final StudentServiceRepository studentServiceRepository;

    public Long save(StudentService studentService) {
        return studentServiceRepository.save(studentService).getId();
    }

    public Optional<StudentService> readById(Long id) {
        return studentServiceRepository.findById(id);
    }

    public Page<StudentService> readByStatus(StudentServiceStatus status, Pageable pageable) {
        return studentServiceRepository.findAllByStatus(status, pageable);
    }

    public Page<StudentService> readByStatusAndContentType(
            StudentServiceStatus status,
            StudentServiceContentType contentType,
            Pageable pageable
    ) {
        if (contentType == StudentServiceContentType.SERVICE) {
            return studentServiceRepository
                    .findAllByStatusAndContentTypeOrStatusAndContentTypeIsNull(
                            status,
                            contentType,
                            status,
                            pageable
                    );
        }
        return studentServiceRepository.findAllByStatusAndContentType(
                status,
                contentType,
                pageable
        );
    }

    public Page<StudentService> readAll(Pageable pageable) {
        return studentServiceRepository.findAll(pageable);
    }

    public Page<StudentService> readByUserId(Long userId, Pageable pageable) {
        return studentServiceRepository.findAllByUser_Id(userId, pageable);
    }

    public List<StudentService> readTop3PopularSince(LocalDate startDate) {
        return studentServiceRepository.findTop3PopularSince(startDate);
    }

    public List<StudentService> readTop3Latest(StudentServiceStatus status) {
        return studentServiceRepository.findLatest(status, PageRequest.of(0, 3));
    }

    public List<StudentService> readTop3LatestSince(
            StudentServiceStatus status,
            LocalDateTime startAt
    ) {
        return studentServiceRepository.findLatestSince(status, startAt, PageRequest.of(0, 3));
    }

    public List<StudentService> lockAllApprovedForPromotion() {
        return studentServiceRepository.findAllApprovedForPromotionUpdate(StudentServiceStatus.APPROVED);
    }

    public void deleteById(Long id) {
        studentServiceRepository.deleteById(id);
    }

    public void increaseClickCount(Long studentServiceId) {
        studentServiceRepository.increaseClickCount(studentServiceId);
    }

    public void increaseViewCount(Long studentServiceId) {
        studentServiceRepository.increaseViewCount(studentServiceId);
    }

    public void increaseLikeCount(Long studentServiceId) {
        studentServiceRepository.increaseLikeCount(studentServiceId);
    }

    public void decreaseLikeCount(Long studentServiceId) {
        studentServiceRepository.decreaseLikeCount(studentServiceId);
    }
}
