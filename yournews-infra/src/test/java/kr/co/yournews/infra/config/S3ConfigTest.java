package kr.co.yournews.infra.config;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

class S3ConfigTest {

    @Test
    void createsS3ClientWithoutApacheHttpClient() {
        try (S3Client s3Client = new S3Config().s3Client(
                "test-access-key",
                "test-secret-key",
                "ap-northeast-2"
        )) {
            assertThat(s3Client).isNotNull();
        }
    }
}
