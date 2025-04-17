package kr.co.yournews.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.auth.dto.AuthCodeDto;
import kr.co.yournews.apis.auth.service.mail.AuthCodeManager;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.exception.UserErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

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
    @DisplayName("인증 코드 요청 - 이미 존재하는 이메일")
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
    @DisplayName("인증 코드 요청 - 너무 빠른 요청")
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
    @DisplayName("인증 코드 검증 - 만료된 코드")
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
    @DisplayName("인증 코드 검증 - 코드 불일치")
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
