package kr.co.yournews.apis.studentservice.service;

import kr.co.yournews.apis.studentservice.dto.StudentServiceDto;
import kr.co.yournews.apis.studentservice.event.StudentServiceRegisteredEvent;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.entity.StudentServiceLike;
import kr.co.yournews.domain.studentservice.exception.StudentServiceErrorType;
import kr.co.yournews.domain.studentservice.service.StudentServiceLikeService;
import kr.co.yournews.domain.studentservice.service.StudentServiceService;
import kr.co.yournews.domain.studentservice.service.StudentServiceDailyStatService;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.infra.redis.RedisRepository;
import kr.co.yournews.infra.redis.util.RedisConstants;
import kr.co.yournews.infra.studentservice.storage.StudentServiceImageStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceCommandService {
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private final UserService userService;
    private final StudentServiceService studentServiceService;
    private final StudentServiceLikeService studentServiceLikeService;
    private final StudentServiceDailyStatService studentServiceDailyStatService;
    private final StudentServiceImageStorage studentServiceImageStorage;
    private final RedisRepository redisRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public StudentServiceDto.Response createStudentService(Long userId, StudentServiceDto.Request request) {
        return createStudentService(userId, request, List.of());
    }

    @Transactional
    public StudentServiceDto.Response createStudentService(
            Long userId,
            StudentServiceDto.Request request,
            List<MultipartFile> images
    ) {
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        List<String> imageUrls = studentServiceImageStorage.uploadAll(images);
        registerS3RollbackCleanup(imageUrls);

        StudentService studentService = request.toEntity(user, imageUrls);
        Long studentServiceId = studentServiceService.save(studentService);
        eventPublisher.publishEvent(new StudentServiceRegisteredEvent(
                studentServiceId,
                request.name(),
                request.description(),
                request.serviceUrls(),
                request.contentType(),
                user.getNickname(),
                user.getEmail(),
                imageUrls.size()
        ));

        return StudentServiceDto.Response.of(studentServiceId);
    }

    @Transactional
    public void updateStudentService(
            Long userId,
            Long studentServiceId,
            StudentServiceDto.UpdateRequest request,
            List<MultipartFile> images
    ) {
        StudentService studentService = studentServiceService.readById(studentServiceId)
                .orElseThrow(() -> new CustomException(StudentServiceErrorType.NOT_FOUND));

        if (!studentService.isAuthor(userId)) {
            throw new CustomException(StudentServiceErrorType.FORBIDDEN);
        }
        if (!studentService.isPending()) {
            throw new CustomException(StudentServiceErrorType.NOT_EDITABLE);
        }

        List<String> currentImageUrls = List.copyOf(studentService.getImageUrls());
        List<String> retainedImageUrls = resolveRetainedImageUrls(
                currentImageUrls,
                request.retainedImageUrls()
        );

        if (retainedImageUrls.size() + countImages(images) > StudentServiceImageStorage.MAX_IMAGE_COUNT) {
            throw new CustomException(StudentServiceErrorType.TOO_MANY_IMAGES);
        }

        List<String> uploadedImageUrls = studentServiceImageStorage.uploadAll(images);
        registerS3RollbackCleanup(uploadedImageUrls);

        List<String> updatedImageUrls = new java.util.ArrayList<>(retainedImageUrls);
        updatedImageUrls.addAll(uploadedImageUrls);

        List<String> removedImageUrls = currentImageUrls.stream()
                .filter(imageUrl -> !retainedImageUrls.contains(imageUrl))
                .toList();

        studentService.update(
                request.name(),
                request.description(),
                request.serviceUrls(),
                request.contentType(),
                updatedImageUrls
        );
        registerS3DeletionAfterCommit(removedImageUrls);
    }

    @Transactional
    public void reportStudentService(Long studentServiceId) {
        StudentService studentService = studentServiceService.readById(studentServiceId)
                .orElseThrow(() -> new CustomException(StudentServiceErrorType.NOT_FOUND));

        studentService.report();
    }

    @Transactional
    public void clickStudentService(Long studentServiceId, Long userId) {
        readApprovedStudentService(studentServiceId);
        String key = RedisConstants.STUDENT_SERVICE_CLICK_PREFIX + studentServiceId + "::" + userId;

        if (redisRepository.setIfAbsent(key, "1", getDurationUntilMidnight())) {
            registerRedisRollbackCleanup(key);
            try {
                studentServiceService.increaseClickCount(studentServiceId);
                studentServiceDailyStatService.increaseClickCount(studentServiceId, today());
            } catch (RuntimeException exception) {
                deleteRedisKeyQuietly(key);
                throw exception;
            }
        }
    }

    @Transactional
    public boolean viewStudentService(Long studentServiceId, Long userId) {
        readApprovedStudentService(studentServiceId);
        String key = RedisConstants.STUDENT_SERVICE_VIEW_PREFIX + studentServiceId + "::" + userId;

        if (redisRepository.setIfAbsent(key, "1", getDurationUntilMidnight())) {
            registerRedisRollbackCleanup(key);
            try {
                studentServiceService.increaseViewCount(studentServiceId);
                studentServiceDailyStatService.increaseViewCount(studentServiceId, today());
            } catch (RuntimeException exception) {
                deleteRedisKeyQuietly(key);
                throw exception;
            }
            return true;
        }
        return false;
    }

    @Transactional
    public void likeStudentService(Long userId, Long studentServiceId) {
        if (studentServiceLikeService.existsByUserIdAndStudentServiceId(userId, studentServiceId)) {
            throw new CustomException(StudentServiceErrorType.ALREADY_LIKED);
        }

        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        StudentService studentService = readApprovedStudentService(studentServiceId);
        StudentServiceLike studentServiceLike = StudentServiceLike.builder()
                .user(user)
                .studentService(studentService)
                .build();

        try {
            studentServiceLikeService.saveAndFlush(studentServiceLike);
        } catch (DataIntegrityViolationException exception) {
            throw new CustomException(StudentServiceErrorType.ALREADY_LIKED);
        }
        studentServiceService.increaseLikeCount(studentServiceId);
        studentServiceDailyStatService.increaseLikeCount(studentServiceId, today());
    }

    @Transactional
    public void unlikeStudentService(Long userId, Long studentServiceId) {
        readApprovedStudentService(studentServiceId);

        if (studentServiceLikeService.deleteByUserIdAndStudentServiceId(userId, studentServiceId)) {
            studentServiceService.decreaseLikeCount(studentServiceId);
            studentServiceDailyStatService.decreaseLikeCount(studentServiceId, today());
        }
    }

    @Transactional
    public void deleteStudentService(Long userId, Long studentServiceId) {
        StudentService studentService = studentServiceService.readById(studentServiceId)
                .orElseThrow(() -> new CustomException(StudentServiceErrorType.NOT_FOUND));

        if (!studentService.isAuthor(userId)) {
            throw new CustomException(StudentServiceErrorType.FORBIDDEN);
        }

        List<String> imageUrls = List.copyOf(studentService.getImageUrls());

        studentServiceDailyStatService.deleteAllByStudentServiceId(studentServiceId);
        studentServiceService.deleteById(studentServiceId);
        registerS3DeletionAfterCommit(imageUrls);
    }

    private StudentService readApprovedStudentService(Long studentServiceId) {
        return studentServiceService.readById(studentServiceId)
                .filter(service -> service.getStatus() == StudentServiceStatus.APPROVED)
                .orElseThrow(() -> new CustomException(StudentServiceErrorType.NOT_FOUND));
    }

    private Duration getDurationUntilMidnight() {
        ZonedDateTime now = ZonedDateTime.now(KOREA_ZONE_ID);
        ZonedDateTime midnight = now.toLocalDate()
                .plusDays(1)
                .atStartOfDay(KOREA_ZONE_ID);
        return Duration.between(now, midnight);
    }

    private LocalDate today() {
        return LocalDate.now(KOREA_ZONE_ID);
    }

    private void registerRedisRollbackCleanup(String key) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    deleteRedisKeyQuietly(key);
                }
            }
        });
    }

    private void deleteRedisKeyQuietly(String key) {
        try {
            redisRepository.del(key);
        } catch (RuntimeException exception) {
            log.warn("Redis 조회수 중복 방지 키 삭제에 실패했습니다. key={}", key, exception);
        }
    }

    private List<String> resolveRetainedImageUrls(
            List<String> currentImageUrls,
            List<String> requestedImageUrls
    ) {
        if (requestedImageUrls == null) {
            return currentImageUrls;
        }

        if (new HashSet<>(requestedImageUrls).size() != requestedImageUrls.size()
                || !currentImageUrls.containsAll(requestedImageUrls)) {
            throw new CustomException(StudentServiceErrorType.INVALID_IMAGE_SELECTION);
        }

        return List.copyOf(requestedImageUrls);
    }

    private long countImages(List<MultipartFile> images) {
        if (images == null) {
            return 0;
        }

        return images.stream()
                .filter(image -> image != null && !image.isEmpty())
                .count();
    }

    private void registerS3RollbackCleanup(List<String> imageUrls) {
        if (imageUrls.isEmpty() || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    studentServiceImageStorage.deleteAllQuietly(imageUrls);
                }
            }
        });
    }

    private void registerS3DeletionAfterCommit(List<String> imageUrls) {
        if (imageUrls.isEmpty() || !TransactionSynchronizationManager.isSynchronizationActive()) {
            if (!imageUrls.isEmpty()) {
                studentServiceImageStorage.deleteAllQuietly(imageUrls);
            }
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                studentServiceImageStorage.deleteAllQuietly(imageUrls);
            }
        });
    }
}
