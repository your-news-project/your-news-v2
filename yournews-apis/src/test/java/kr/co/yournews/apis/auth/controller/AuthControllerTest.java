package kr.co.yournews.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.auth.dto.RestoreUserDto;
import kr.co.yournews.apis.auth.service.AuthCommandService;
import kr.co.yournews.auth.dto.SignInDto;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.common.exception.AuthErrorType;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthCommandService authCommandService;

    private TokenDto tokenDto;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        createToken();
    }

    private void createToken() {
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        tokenDto = TokenDto.of(accessToken, refreshToken);
    }

    @Nested
    @DisplayName("회원가입")
    class SignUpTest {

        @Test
        @DisplayName("성공")
        void signUpSuccess() throws Exception {
            // given
            SignUpDto.Auth signUpDto =
                    new SignUpDto.Auth("test123", "password1234@", "테스터",
                            "test@naver.com", List.of(1L, 2L, 3L), List.of("키워드1", "키워드2"), true, true);

            given(authCommandService.signUp(signUpDto)).willReturn(tokenDto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/sign-up")
                            .content(objectMapper.writeValueAsBytes(signUpDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."))
                    .andExpect(jsonPath("$.data.accessToken").value(tokenDto.accessToken()))
                    .andExpect(jsonPath("$.data.refreshToken").value(tokenDto.refreshToken()));
        }

        @Test
        @DisplayName("실패 - 유효성 검사 실패")
        void signUpInvalidFailed() throws Exception {
            // given
            SignUpDto.Auth signUpDto =
                    new SignUpDto.Auth(null, null, null,
                            null, List.of(1L, 2L, 3L), List.of("키워드1", "키워드2"), true, true);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/sign-up")
                            .content(objectMapper.writeValueAsBytes(signUpDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.username").value("아이디는 필수 입력입니다."))
                    .andExpect(jsonPath("$.errors.password").value("비밀번호는 필수 입력 값입니다."))
                    .andExpect(jsonPath("$.errors.nickname").value("닉네임은 필수 입력 값입니다."))
                    .andExpect(jsonPath("$.errors.email").value("이메일은 필수 입력 값입니다."));
        }

        @Test
        @DisplayName("실패 - 조건 불충족")
        void signUpInsufficient() throws Exception {
            // given
            SignUpDto.Auth signUpDto =
                    new SignUpDto.Auth("te", "password", "x",
                    "testgmail.com", List.of(1L, 2L, 3L), List.of("키워드1", "키워드2"), true, true);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/sign-up")
                            .content(objectMapper.writeValueAsBytes(signUpDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.username").value("아이디는 특수문자를 제외한 4~20자리여야 합니다."))
                    .andExpect(jsonPath("$.errors.password").value("비밀번호는 8~16자 영문, 숫자, 특수문자를 사용하세요."))
                    .andExpect(jsonPath("$.errors.nickname").value("닉네임은 특수문자를 제외한 2~10자리여야 합니다."))
                    .andExpect(jsonPath("$.errors.email").value("이메일 형식이 아닙니다."));
        }

        @Test
        @DisplayName("실패 - 인증 코드 검증되지 않음")
        void signUpCodeNotVerified() throws Exception {
            // given
            SignUpDto.Auth signUpDto =
                    new SignUpDto.Auth("test123", "password1234@", "테스터",
                            "test@naver.com", List.of(1L, 2L, 3L), List.of("키워드1", "키워드2"), true, true);

            doThrow(new CustomException(UserErrorType.CODE_NOT_VERIFIED))
                    .when(authCommandService).signUp(signUpDto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/sign-up")
                            .content(objectMapper.writeValueAsBytes(signUpDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value(UserErrorType.CODE_NOT_VERIFIED.getMessage()))
                    .andExpect(jsonPath("$.code").value(UserErrorType.CODE_NOT_VERIFIED.getCode()));
        }
    }

    @Nested
    @DisplayName("로그인")
    class SignInTest {

        private final SignInDto signInDto = new SignInDto("test", "pass1234@");

        @Test
        @DisplayName("성공")
        void signInSuccess() throws Exception {
            // given
            given(authCommandService.signIn(signInDto)).willReturn(tokenDto);

            // then
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/sign-in")
                            .content(objectMapper.writeValueAsBytes(signInDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."))
                    .andExpect(jsonPath("$.data.accessToken").value(tokenDto.accessToken()))
                    .andExpect(jsonPath("$.data.refreshToken").value(tokenDto.refreshToken()));
        }

        @Test
        @DisplayName("실패 - 없는 사용자")
        void signInFailedByUserNotFound() throws Exception {
            // given
            given(authCommandService.signIn(signInDto))
                    .willThrow(new CustomException(UserErrorType.NOT_FOUND));

            // then
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/sign-in")
                            .content(objectMapper.writeValueAsBytes(signInDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(UserErrorType.NOT_FOUND.getMessage()))
                    .andExpect(jsonPath("$.code").value(UserErrorType.NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void signInFailedByMisMatchPass() throws Exception {
            // given
            given(authCommandService.signIn(signInDto))
                    .willThrow(new CustomException(UserErrorType.NOT_MATCHED_PASSWORD));

            // then
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/sign-in")
                            .content(objectMapper.writeValueAsBytes(signInDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(UserErrorType.NOT_MATCHED_PASSWORD.getMessage()))
                    .andExpect(jsonPath("$.code").value(UserErrorType.NOT_MATCHED_PASSWORD.getCode()));
        }

        @Test
        @DisplayName("실패 - Soft Deleted User")
        void signInFailedBySoftDeletedUser() throws Exception {
            // given
            given(authCommandService.signIn(signInDto))
                    .willThrow(new CustomException(UserErrorType.DEACTIVATED));

            // then
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/sign-in")
                            .content(objectMapper.writeValueAsBytes(signInDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value(UserErrorType.DEACTIVATED.getMessage()))
                    .andExpect(jsonPath("$.code").value(UserErrorType.DEACTIVATED.getCode()));
        }
    }

    @Nested
    @DisplayName("계정 복구 테스트")
    class RestoreUserTest {

        private final RestoreUserDto.Request request = new RestoreUserDto.Request("username");

        @Test
        @DisplayName("성공")
        void restoreDeletedUserSuccess() throws Exception {
            // given
            given(authCommandService.restoreUser(any())).willReturn(tokenDto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/restore")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."))
                    .andExpect(jsonPath("$.data.accessToken").value(tokenDto.accessToken()))
                    .andExpect(jsonPath("$.data.refreshToken").value(tokenDto.refreshToken()));
        }

        @Test
        @DisplayName("실패 - 이미 활성화된 사용자")
        void restoreAlreadyActiveUserFail() throws Exception {
            // given
            given(authCommandService.restoreUser(request)).willThrow(
                    new CustomException(UserErrorType.ALREADY_ACTIVE)
            );

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/restore")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
            );

            resultActions
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(UserErrorType.ALREADY_ACTIVE.getMessage()))
                    .andExpect(jsonPath("$.code").value(UserErrorType.ALREADY_ACTIVE.getCode()));
        }
    }

    @Nested
    @DisplayName("토큰 재발급")
    class ReIssueTest {

        @Test
        @DisplayName("성공")
        void reIssueTokenSuccess() throws Exception {
            // given
            String reIssueRefreshToken = "reIssueRefreshToken";
            String reIssueAccessToken = "reIssueAccessToken";
            TokenDto newTokenDto = TokenDto.of(reIssueAccessToken, reIssueRefreshToken);

            given(authCommandService.reissueAccessToken(tokenDto.refreshToken())).willReturn(newTokenDto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/reissue")
                            .header("X-Refresh-Token", tokenDto.refreshToken())
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value(newTokenDto.accessToken()))
                    .andExpect(jsonPath("$.data.refreshToken").value(newTokenDto.refreshToken()));
        }

        @Test
        @DisplayName("실패 - Refresh Token 없음")
        void reIssueTokenException() throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/auth/reissue")
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(AuthErrorType.REFRESH_TOKEN_NOT_FOUND.getMessage()))
                    .andExpect(jsonPath("$.code").value(AuthErrorType.REFRESH_TOKEN_NOT_FOUND.getCode()));
        }
    }
}
