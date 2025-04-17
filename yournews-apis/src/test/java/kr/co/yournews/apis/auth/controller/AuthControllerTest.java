package kr.co.yournews.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.yournews.apis.auth.service.AuthCommandService;
import kr.co.yournews.auth.dto.SignInDto;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.common.exception.AuthErrorType;
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

import static kr.co.yournews.common.util.AuthConstants.AUTHORIZATION;
import static kr.co.yournews.common.util.AuthConstants.REFRESH_TOKEN_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
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

    @Test
    @DisplayName("회원가입 테스트")
    void signUpTest() throws Exception {
        // given
        SignUpDto.Auth signUpDto = new SignUpDto.Auth("test123", "password1234@",
                "테스터", "test@naver.com");

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
                .andExpect(jsonPath("$.data.accessToken").value(tokenDto.accessToken()));
    }
    @Test
    @DisplayName("회원가입 테스트 - 유효성 검사 실패")
    void signUpInvalidFailedTest() throws Exception {
        // given
        SignUpDto.Auth signUpDto = new SignUpDto.Auth(null, null, null, null);

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
    @DisplayName("회원가입 테스트 - 조건 불충족")
    void signUpInsufficientTest() throws Exception {
        // given
        SignUpDto.Auth signUpDto = new SignUpDto.Auth("te", "password", "x",
                "testgmail.com");

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
    @DisplayName("로그인 테스트")
    void signInTest() throws Exception {
        // given
        SignInDto signInDto = new SignInDto("test", "pass1234@");

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
                .andExpect(jsonPath("$.data.accessToken").value(tokenDto.accessToken()));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 없는 사용자")
    void signInFailedByUserNotFoundTest() throws Exception {
        // given
        SignInDto signInDto = new SignInDto("test", "pass1234@");

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
    @DisplayName("로그인 실패 테스트 - 비밀번호 불일치")
    void signInFailedByMisMatchPassTest() throws Exception {
        // given
        SignInDto signInDto = new SignInDto("test", "pass1234@");

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
    @DisplayName("AccessToken 재발급 테스트")
    void reIssueTokenTest() throws Exception {
        // given
        String reIssueRefreshToken = "reIssueRefreshToken";
        String reIssueAccessToken = "reIssueAccessToken";
        TokenDto newTokenDto = TokenDto.of(reIssueAccessToken, reIssueRefreshToken);

        given(authCommandService.reissueAccessToken(tokenDto.refreshToken())).willReturn(newTokenDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/auth/reissue")
                        .cookie(new Cookie(REFRESH_TOKEN_KEY, tokenDto.refreshToken()))
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(newTokenDto.accessToken()))
                .andExpect(cookie().exists(REFRESH_TOKEN_KEY));
    }

    @Test
    @DisplayName("AccessToken 재발급 테스트 - Refresh Token 없음")
    void reIssueTokenExceptionTest() throws Exception {
        // given

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

    @Test
    @DisplayName("로그아웃 테스트")
    void signOutTest() throws Exception {
        // given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        doNothing().when(authCommandService).signOut(eq(accessToken), eq(refreshToken), any(HttpServletResponse.class));

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/auth/sign-out")
                        .cookie(new Cookie(REFRESH_TOKEN_KEY, tokenDto.refreshToken()))
                        .header(AUTHORIZATION, accessToken)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
    }
}
