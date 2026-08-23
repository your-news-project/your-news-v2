package kr.co.yournews.domain.studentservice.entity;

import kr.co.yournews.domain.studentservice.type.StudentServiceStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentServiceTest {

    @Test
    void rejectStoresTrimmedReason() {
        StudentService studentService = createStudentService();

        studentService.reject("  등록한 링크에 접속할 수 없습니다.  ");

        assertEquals(StudentServiceStatus.REJECTED, studentService.getStatus());
        assertEquals("등록한 링크에 접속할 수 없습니다.", studentService.getRejectionReason());
    }

    @Test
    void rejectRequiresReason() {
        StudentService studentService = createStudentService();

        assertThrows(IllegalArgumentException.class, () -> studentService.reject("   "));
    }

    @Test
    void approveClearsPreviousRejectionReason() {
        StudentService studentService = createStudentService();
        studentService.reject("내용을 보완해주세요.");

        studentService.approve();

        assertEquals(StudentServiceStatus.APPROVED, studentService.getStatus());
        assertNull(studentService.getRejectionReason());
    }

    private StudentService createStudentService() {
        return StudentService.builder()
                .name("동아리 모집")
                .description("함께할 학생을 찾아요")
                .serviceUrls(List.of("https://example.com"))
                .imageUrls(List.of())
                .user(null)
                .build();
    }
}
