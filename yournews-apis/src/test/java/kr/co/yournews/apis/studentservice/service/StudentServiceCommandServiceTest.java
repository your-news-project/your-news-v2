package kr.co.yournews.apis.studentservice.service;

import kr.co.yournews.apis.studentservice.dto.StudentServiceDto;
import kr.co.yournews.apis.studentservice.event.StudentServiceRegisteredEvent;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.studentservice.entity.StudentService;
import kr.co.yournews.domain.studentservice.exception.StudentServiceErrorType;
import kr.co.yournews.domain.studentservice.service.StudentServiceLikeService;
import kr.co.yournews.domain.studentservice.service.StudentServiceService;
import kr.co.yournews.domain.studentservice.service.StudentServiceDailyStatService;
import kr.co.yournews.domain.studentservice.type.StudentServiceContentType;
import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.service.UserService;
import kr.co.yournews.infra.redis.RedisRepository;
import kr.co.yournews.infra.studentservice.storage.StudentServiceImageStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceCommandServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private StudentServiceService studentServiceService;
    @Mock
    private StudentServiceLikeService studentServiceLikeService;
    @Mock
    private StudentServiceDailyStatService studentServiceDailyStatService;
    @Mock
    private StudentServiceImageStorage studentServiceImageStorage;
    @Mock
    private RedisRepository redisRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private StudentServiceCommandService commandService;

    @Test
    void createsPendingServiceWithImagesAndPublishesNotificationEvent() {
        User user = User.builder()
                .nickname("등록자")
                .email("student@example.com")
                .build();
        StudentServiceDto.Request request = new StudentServiceDto.Request(
                "테스트 서비스",
                "서비스 소개",
                List.of("https://example.com"),
                StudentServiceContentType.SERVICE
        );
        MockMultipartFile image = new MockMultipartFile(
                "images", "image.jpg", "image/jpeg", new byte[]{1}
        );

        when(userService.readById(1L)).thenReturn(Optional.of(user));
        when(studentServiceService.save(any(StudentService.class))).thenReturn(10L);
        when(studentServiceImageStorage.uploadAll(List.of(image)))
                .thenReturn(List.of("https://bucket.s3.ap-northeast-2.amazonaws.com/student-services/image.jpg"));

        StudentServiceDto.Response response = commandService.createStudentService(
                1L,
                request,
                List.of(image)
        );

        assertThat(response.id()).isEqualTo(10L);
        ArgumentCaptor<StudentService> serviceCaptor = ArgumentCaptor.forClass(StudentService.class);
        verify(studentServiceService).save(serviceCaptor.capture());
        assertThat(serviceCaptor.getValue().getImageUrls()).containsExactly(
                "https://bucket.s3.ap-northeast-2.amazonaws.com/student-services/image.jpg"
        );

        ArgumentCaptor<StudentServiceRegisteredEvent> eventCaptor =
                ArgumentCaptor.forClass(StudentServiceRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().studentServiceId()).isEqualTo(10L);
        assertThat(eventCaptor.getValue().imageCount()).isEqualTo(1);
    }

    @Test
    void updatesPendingServiceAndSynchronizesChangedImages() {
        String removedUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/student-services/old-1.jpg";
        String retainedUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/student-services/old-2.jpg";
        String newUrl = "https://bucket.s3.ap-northeast-2.amazonaws.com/student-services/new.jpg";
        User user = User.builder().build();
        StudentService studentService = StudentService.builder()
                .name("기존 이름")
                .description("기존 소개")
                .imageUrls(List.of(removedUrl, retainedUrl))
                .user(user)
                .build();
        ReflectionTestUtils.setField(studentService, "userId", 1L);
        StudentServiceDto.UpdateRequest request = new StudentServiceDto.UpdateRequest(
                "수정 이름",
                "수정 소개",
                List.of("https://updated.example.com"),
                StudentServiceContentType.SERVICE,
                List.of(retainedUrl)
        );
        MockMultipartFile newImage = new MockMultipartFile(
                "images", "new.jpg", "image/jpeg", new byte[]{1}
        );

        when(studentServiceService.readById(10L)).thenReturn(Optional.of(studentService));
        when(studentServiceImageStorage.uploadAll(List.of(newImage))).thenReturn(List.of(newUrl));

        commandService.updateStudentService(1L, 10L, request, List.of(newImage));

        assertThat(studentService.getName()).isEqualTo("수정 이름");
        assertThat(studentService.getDescription()).isEqualTo("수정 소개");
        assertThat(studentService.getImageUrls()).containsExactly(retainedUrl, newUrl);
        verify(studentServiceImageStorage).deleteAllQuietly(List.of(removedUrl));
    }

    @Test
    void doesNotUpdateApprovedService() {
        StudentService studentService = StudentService.builder()
                .name("승인 서비스")
                .description("소개")
                .imageUrls(List.of())
                .user(User.builder().build())
                .build();
        ReflectionTestUtils.setField(studentService, "userId", 1L);
        studentService.approve();
        StudentServiceDto.UpdateRequest request = new StudentServiceDto.UpdateRequest(
                "수정 이름",
                "수정 소개",
                List.of(),
                StudentServiceContentType.SERVICE,
                null
        );

        when(studentServiceService.readById(10L)).thenReturn(Optional.of(studentService));

        assertThatThrownBy(() -> commandService.updateStudentService(
                1L, 10L, request, List.of()
        )).isInstanceOfSatisfying(CustomException.class, exception ->
                assertThat(exception.getErrorType()).isEqualTo(StudentServiceErrorType.NOT_EDITABLE)
        );
    }

    @Test
    void deletesStoredImagesWhenAuthorDeletesService() {
        List<String> imageUrls = List.of(
                "https://bucket.s3.ap-northeast-2.amazonaws.com/student-services/one.jpg",
                "https://bucket.s3.ap-northeast-2.amazonaws.com/student-services/two.jpg"
        );
        StudentService studentService = StudentService.builder()
                .name("삭제 서비스")
                .description("소개")
                .imageUrls(imageUrls)
                .user(User.builder().build())
                .build();
        ReflectionTestUtils.setField(studentService, "userId", 1L);
        when(studentServiceService.readById(10L)).thenReturn(Optional.of(studentService));

        commandService.deleteStudentService(1L, 10L);

        verify(studentServiceService).deleteById(10L);
        verify(studentServiceImageStorage).deleteAllQuietly(imageUrls);
    }

    @Test
    void incrementsClickCountInDatabaseWhenRedisMarkerIsCreated() {
        StudentService studentService = approvedStudentService();
        when(studentServiceService.readById(10L)).thenReturn(Optional.of(studentService));
        when(redisRepository.setIfAbsent(eq("student-service:click::10::1"), eq("1"), any(Duration.class)))
                .thenReturn(true);

        commandService.clickStudentService(10L, 1L);

        verify(studentServiceService).increaseClickCount(10L);
    }

    @Test
    void doesNotIncrementClickCountWhenRedisMarkerAlreadyExists() {
        StudentService studentService = approvedStudentService();
        when(studentServiceService.readById(10L)).thenReturn(Optional.of(studentService));
        when(redisRepository.setIfAbsent(eq("student-service:click::10::1"), eq("1"), any(Duration.class)))
                .thenReturn(false);

        commandService.clickStudentService(10L, 1L);

        verify(studentServiceService, never()).increaseClickCount(any());
    }

    @Test
    void incrementsViewCountOncePerUserWhenDailyRedisMarkerIsCreated() {
        StudentService studentService = approvedStudentService();
        when(studentServiceService.readById(10L)).thenReturn(Optional.of(studentService));
        when(redisRepository.setIfAbsent(eq("student-service:view::10::1"), eq("1"), any(Duration.class)))
                .thenReturn(true);

        boolean increased = commandService.viewStudentService(10L, 1L);

        assertThat(increased).isTrue();
        verify(studentServiceService).increaseViewCount(10L);
    }

    @Test
    void doesNotIncrementViewCountWhenUserAlreadyViewedToday() {
        StudentService studentService = approvedStudentService();
        when(studentServiceService.readById(10L)).thenReturn(Optional.of(studentService));
        when(redisRepository.setIfAbsent(eq("student-service:view::10::1"), eq("1"), any(Duration.class)))
                .thenReturn(false);

        boolean increased = commandService.viewStudentService(10L, 1L);

        assertThat(increased).isFalse();
        verify(studentServiceService, never()).increaseViewCount(any());
    }

    @Test
    void storesLikeAndIncrementsCountOnlyInDatabase() {
        User user = User.builder().build();
        StudentService studentService = approvedStudentService();
        when(studentServiceLikeService.existsByUserIdAndStudentServiceId(1L, 10L)).thenReturn(false);
        when(userService.readById(1L)).thenReturn(Optional.of(user));
        when(studentServiceService.readById(10L)).thenReturn(Optional.of(studentService));

        commandService.likeStudentService(1L, 10L);

        verify(studentServiceLikeService).saveAndFlush(any());
        verify(studentServiceService).increaseLikeCount(10L);
        verify(redisRepository, never()).setIfAbsent(anyString(), any(), any(Duration.class));
    }

    @Test
    void decrementsCountOnlyWhenLikeRowWasDeleted() {
        StudentService studentService = approvedStudentService();
        when(studentServiceService.readById(10L)).thenReturn(Optional.of(studentService));
        when(studentServiceLikeService.deleteByUserIdAndStudentServiceId(1L, 10L)).thenReturn(true);

        commandService.unlikeStudentService(1L, 10L);

        verify(studentServiceService).decreaseLikeCount(10L);
        verify(redisRepository, never()).del(anyString());
    }

    private StudentService approvedStudentService() {
        StudentService studentService = StudentService.builder()
                .name("승인 서비스")
                .description("소개")
                .imageUrls(List.of())
                .user(User.builder().build())
                .build();
        ReflectionTestUtils.setField(studentService, "status", StudentServiceStatus.APPROVED);
        return studentService;
    }
}
