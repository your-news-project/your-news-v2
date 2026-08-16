package kr.co.yournews.infra.studentservice.storage;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.studentservice.exception.StudentServiceErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentServiceImageStorageTest {

    @Mock
    private S3Client s3Client;

    private StudentServiceImageStorage imageStorage;

    @BeforeEach
    void setUp() {
        imageStorage = new StudentServiceImageStorage(s3Client);
        ReflectionTestUtils.setField(imageStorage, "bucket", "test-bucket");
        ReflectionTestUtils.setField(imageStorage, "region", "ap-northeast-2");
    }

    @Test
    void uploadsValidPngImage() {
        byte[] png = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00
        };
        MockMultipartFile image = new MockMultipartFile(
                "images", "image.png", "image/png", png
        );

        List<String> imageUrls = imageStorage.uploadAll(List.of(image));

        assertThat(imageUrls).hasSize(1);
        assertThat(imageUrls.get(0))
                .startsWith("https://test-bucket.s3.ap-northeast-2.amazonaws.com/student-services/")
                .endsWith(".png");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void rejectsMoreThanThreeImages() {
        MockMultipartFile image = validJpeg();

        assertThatThrownBy(() -> imageStorage.uploadAll(List.of(image, image, image, image)))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                        assertThat(exception.getErrorType())
                                .isEqualTo(StudentServiceErrorType.TOO_MANY_IMAGES)
                );
    }

    @Test
    void rejectsFileWhoseSignatureDoesNotMatchContentType() {
        MockMultipartFile image = new MockMultipartFile(
                "images", "fake.jpg", "image/jpeg", "not-an-image".getBytes()
        );

        assertThatThrownBy(() -> imageStorage.uploadAll(List.of(image)))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                        assertThat(exception.getErrorType())
                                .isEqualTo(StudentServiceErrorType.INVALID_IMAGE_FORMAT)
                );
    }

    private MockMultipartFile validJpeg() {
        return new MockMultipartFile(
                "images",
                "image.jpg",
                "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}
        );
    }
}
