package kr.co.yournews.admin.studentservice.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentServiceManagementDtoTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectRequestRequiresReason() {
        StudentServiceManagementDto.RejectRequest request =
                new StudentServiceManagementDto.RejectRequest("   ");

        assertEquals(1, validator.validate(request).size());
    }

    @Test
    void rejectRequestLimitsReasonToFiveHundredCharacters() {
        StudentServiceManagementDto.RejectRequest request =
                new StudentServiceManagementDto.RejectRequest("가".repeat(501));

        assertEquals(1, validator.validate(request).size());
    }

    @Test
    void rejectRequestAcceptsValidReason() {
        StudentServiceManagementDto.RejectRequest request =
                new StudentServiceManagementDto.RejectRequest("링크에 접속할 수 없습니다.");

        assertTrue(validator.validate(request).isEmpty());
    }
}
