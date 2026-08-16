package kr.co.yournews.domain.studentservice.service;

import kr.co.yournews.domain.studentservice.entity.StudentServiceLike;
import kr.co.yournews.domain.studentservice.repository.StudentServiceLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentServiceLikeService {
    private final StudentServiceLikeRepository studentServiceLikeRepository;

    public void saveAndFlush(StudentServiceLike studentServiceLike) {
        studentServiceLikeRepository.saveAndFlush(studentServiceLike);
    }

    public boolean existsByUserIdAndStudentServiceId(Long userId, Long studentServiceId) {
        return studentServiceLikeRepository.existsByUser_IdAndStudentService_Id(userId, studentServiceId);
    }

    public boolean deleteByUserIdAndStudentServiceId(Long userId, Long studentServiceId) {
        return studentServiceLikeRepository.deleteByUserIdAndStudentServiceId(userId, studentServiceId) > 0;
    }
}
