package kr.co.yournews.domain.studentservice.service;

import kr.co.yournews.domain.studentservice.repository.StudentServiceDailyStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StudentServiceDailyStatService {
    private final StudentServiceDailyStatRepository repository;

    public void increaseViewCount(Long studentServiceId, LocalDate statDate) {
        repository.increaseViewCount(studentServiceId, statDate);
    }

    public void increaseClickCount(Long studentServiceId, LocalDate statDate) {
        repository.increaseClickCount(studentServiceId, statDate);
    }

    public void increaseLikeCount(Long studentServiceId, LocalDate statDate) {
        repository.increaseLikeCount(studentServiceId, statDate);
    }

    public void decreaseLikeCount(Long studentServiceId, LocalDate statDate) {
        repository.decreaseLikeCount(studentServiceId, statDate);
    }

    public void deleteAllByStudentServiceId(Long studentServiceId) {
        repository.deleteAllByStudentServiceId(studentServiceId);
    }
}
