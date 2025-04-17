package kr.co.yournews.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.auth.dto.AuthCodeDto;
import kr.co.yournews.apis.auth.dto.PassResetDto;
import kr.co.yournews.apis.auth.service.mail.AuthCodeManager;
import kr.co.yournews.apis.auth.service.mail.PassCodeManager;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.exception.UserErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthSupportController.class)
public class AuthSupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthCodeManager authCodeManager;

    @MockBean
    private PassCodeManager passCodeManager;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Nested
    @DisplayName("이메일 인증 코드")
    class AuthCodeTest {

        @Test
        @DisplayName("인증 코드 요청 - 성공")
        void requestCodeTest() throws Exception {
            // given
            AuthCodeDto.Request dto = new AuthCodeDto.Request("test@email.com");

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/code/request")
                            .content(objectMapper.writeValueAsString(dto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
        }

        @Test
        @DisplayName("인증 코드 요청 실패 - 이미 존재하는 이메일")
        void requestCodeExistEmailTest() throws Exception {
            // given
            AuthCodeDto.Request dto = new AuthCodeDto.Request("exist@email.com");

            doThrow(new CustomException(UserErrorType.EXIST_EMAIL))
                    .when(authCodeManager).sendAuthCode(dto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/code/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(UserErrorType.EXIST_EMAIL.getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorType.EXIST_EMAIL.getMessage()));
        }

        @Test
        @DisplayName("인증 코드 요청 실패 - 너무 빠른 요청")
        void requestCodeTooFastTest() throws Exception {
            // given
            AuthCodeDto.Request dto = new AuthCodeDto.Request("test@email.com");

            doThrow(new CustomException(UserErrorType.ALREADY_MAIL_REQUEST))
                    .when(authCodeManager).sendAuthCode(dto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/code/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.code").value(UserErrorType.ALREADY_MAIL_REQUEST.getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorType.ALREADY_MAIL_REQUEST.getMessage()));
        }

        @Test
        @DisplayName("인증 코드 검증 - 성공")
        void verifyCodeTest() throws Exception {
            // given
            AuthCodeDto.Verify dto = new AuthCodeDto.Verify("test@email.com", "123456");

            when(authCodeManager.verifyAuthCode(dto)).thenReturn(true);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/code/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
        }

        @Test
        @DisplayName("인증 코드 검증 실패 - 만료된 코드")
        void verifyCodeExpiredTest() throws Exception {
            // given
            AuthCodeDto.Verify dto = new AuthCodeDto.Verify("test@email.com", "123456");

            doThrow(new CustomException(UserErrorType.CODE_EXPIRED))
                    .when(authCodeManager).verifyAuthCode(dto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/code/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.code").value(UserErrorType.CODE_EXPIRED.getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorType.CODE_EXPIRED.getMessage()));
        }

        @Test
        @DisplayName("실패 - 코드 불일치")
        void verifyCodeMismatchTest() throws Exception {
            // given
            AuthCodeDto.Verify dto = new AuthCodeDto.Verify("test@email.com", "wrongcode");

            doThrow(new CustomException(UserErrorType.INVALID_CODE))
                    .when(authCodeManager).verifyAuthCode(dto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/code/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(UserErrorType.INVALID_CODE.getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorType.INVALID_CODE.getMessage()));
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정 링크 요청")
    class PasswordResetLinkTest {

        @Test
        @DisplayName("성공")
        void requestResetLinkSuccess() throws Exception {
            // given
            PassResetDto.VerifyUser dto = new PassResetDto.VerifyUser("user1", "user1@email.com");

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/password/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
        }

        @Test
        @DisplayName("실패 - 사용자 정보 불일치")
        void requestResetLinkInvalidUser() throws Exception {
            // given
            PassResetDto.VerifyUser dto = new PassResetDto.VerifyUser("invalidUser", "invalid@email.com");

            doThrow(new CustomException(UserErrorType.INVALID_USER_INFO))
                    .when(passCodeManager).initiatePasswordReset(dto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/password/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
            );

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(UserErrorType.INVALID_USER_INFO.getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorType.INVALID_USER_INFO.getMessage()));
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정 적용")
    class PasswordApplyTest {

        @Test
        @DisplayName("성공")
        void applyNewPasswordSuccess() throws Exception {
            // given
            PassResetDto.ResetPassword dto = new PassResetDto.ResetPassword(
                    "user1", "uuid-1234", "Abcdefg123!"
            );

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/password/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
        }

        @Test
        @DisplayName("실패 - 유효성 실패 (비밀번호 형식)")
        void applyNewPasswordValidationFail() throws Exception {
            // given
            PassResetDto.ResetPassword dto = new PassResetDto.ResetPassword(
                    "user1", "uuid-1234", "123"
            );

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/password/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
            );

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.password").value("비밀번호는 8~16자 영문, 숫자, 특수문자를 사용하세요."));
        }

        @Test
        @DisplayName("실패 - 인증 실패 (uuid 불일치)")
        void applyNewPasswordInvalidUuid() throws Exception {
            // given
            PassResetDto.ResetPassword dto = new PassResetDto.ResetPassword(
                    "user1", "wrong-uuid", "Abcdefg123!"
            );

            doThrow(new CustomException(UserErrorType.UNAUTHORIZED_ACTION))
                    .when(passCodeManager).applyNewPassword(dto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/password/reset")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
            );

            // then
            resultActions
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(UserErrorType.UNAUTHORIZED_ACTION.getCode()))
                    .andExpect(jsonPath("$.message").value(UserErrorType.UNAUTHORIZED_ACTION.getMessage()));
        }
    }
}
