package kr.co.yournews.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PasswordEncodeServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordEncodeService passwordEncodeService;

    @Test
    @DisplayName("비밀번호 인코딩 테스트")
    void passwordEncode() {
        // given
        String password = "password";
        String fakeEncodedPassword = "$2a$10$abcdefg12345678";
        given(passwordEncoder.encode(password)).willReturn(fakeEncodedPassword);

        // when
        String encodedPassword = passwordEncodeService.encode(password);

        // then
        assertThat(encodedPassword).isEqualTo(fakeEncodedPassword);
        assertThat(encodedPassword).isNotEqualTo(password);
    }

    @Test
    @DisplayName("비밀번호 일치 여부 테스트")
    void passwordEqualsTest() {
        // given
        String password = "password";
        String fakeEncodedPassword = "$2a$10$abcdefg12345678";
        given(passwordEncoder.encode(password)).willReturn(fakeEncodedPassword);
        given(passwordEncoder.matches(password, fakeEncodedPassword)).willReturn(true);

        // when
        String encodedPassword = passwordEncodeService.encode(password);

        assertTrue(passwordEncodeService.matches(password, encodedPassword));
    }
}
