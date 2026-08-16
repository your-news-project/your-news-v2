package kr.co.yournews.infra.studentservice.storage;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.studentservice.exception.StudentServiceErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentServiceImageStorage {
    public static final int MAX_IMAGE_COUNT = 3;
    public static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

    private static final String OBJECT_PREFIX = "student-services/";
    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public List<String> uploadAll(List<MultipartFile> images) {
        List<MultipartFile> normalizedImages = images == null
                ? List.of()
                : images.stream().filter(image -> image != null && !image.isEmpty()).toList();

        if (normalizedImages.size() > MAX_IMAGE_COUNT) {
            throw new CustomException(StudentServiceErrorType.TOO_MANY_IMAGES);
        }

        List<String> uploadedUrls = new ArrayList<>();
        try {
            for (MultipartFile image : normalizedImages) {
                uploadedUrls.add(upload(image));
            }
            return List.copyOf(uploadedUrls);
        } catch (RuntimeException e) {
            deleteAllQuietly(uploadedUrls);
            throw e;
        }
    }

    public void deleteAll(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(extractObjectKey(imageUrl))
                    .build());
        }
    }

    private String upload(MultipartFile image) {
        byte[] bytes = readBytes(image);
        String contentType = normalizeContentType(image.getContentType());
        String extension = ALLOWED_CONTENT_TYPES.get(contentType);

        if (extension == null || !hasValidSignature(bytes, contentType)) {
            throw new CustomException(StudentServiceErrorType.INVALID_IMAGE_FORMAT);
        }
        if (bytes.length > MAX_IMAGE_SIZE_BYTES) {
            throw new CustomException(StudentServiceErrorType.IMAGE_TOO_LARGE);
        }

        String objectKey = OBJECT_PREFIX + UUID.randomUUID() + "." + extension;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(bytes));
            return createPublicUrl(objectKey);
        } catch (RuntimeException e) {
            log.error("[학생 서비스 이미지 업로드 실패] objectKey: {}", objectKey, e);
            throw new CustomException(StudentServiceErrorType.IMAGE_UPLOAD_FAILED);
        }
    }

    private byte[] readBytes(MultipartFile image) {
        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new CustomException(StudentServiceErrorType.IMAGE_TOO_LARGE);
        }

        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new CustomException(StudentServiceErrorType.INVALID_IMAGE_FORMAT);
        }
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }

    private boolean hasValidSignature(byte[] bytes, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xFF
                    && (bytes[1] & 0xFF) == 0xD8
                    && (bytes[2] & 0xFF) == 0xFF;
            case "image/png" -> bytes.length >= 8
                    && (bytes[0] & 0xFF) == 0x89
                    && bytes[1] == 0x50
                    && bytes[2] == 0x4E
                    && bytes[3] == 0x47
                    && bytes[4] == 0x0D
                    && bytes[5] == 0x0A
                    && bytes[6] == 0x1A
                    && bytes[7] == 0x0A;
            case "image/webp" -> bytes.length >= 12
                    && bytes[0] == 'R'
                    && bytes[1] == 'I'
                    && bytes[2] == 'F'
                    && bytes[3] == 'F'
                    && bytes[8] == 'W'
                    && bytes[9] == 'E'
                    && bytes[10] == 'B'
                    && bytes[11] == 'P';
            default -> false;
        };
    }

    public void deleteAllQuietly(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(extractObjectKey(imageUrl))
                        .build());
            } catch (RuntimeException cleanupException) {
                log.error("[학생 서비스 이미지 정리 실패] imageUrl: {}", imageUrl, cleanupException);
            }
        }
    }

    private String createPublicUrl(String objectKey) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + objectKey;
    }

    private String extractObjectKey(String imageUrl) {
        String path = URI.create(imageUrl).getPath();
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
