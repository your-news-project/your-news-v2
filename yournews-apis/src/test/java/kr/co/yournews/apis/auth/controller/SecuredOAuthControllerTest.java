package kr.co.yournews.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.auth.dto.OAuthTokenDto;
import kr.co.yournews.apis.auth.service.OAuthCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.auth.dto.SignUpDto;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OAuthController.class)
public class SecuredOAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OAuthCommandService oAuthCommandService;

    private OAuthTokenDto oAuthTokenDto;

    private User user;
    private UserDetails userDetails;
    private final Long userId = 1L;

    @BeforeEach
    void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(post("/**").with(csrf()))
                .build();


        user = User.builder()
                .username("test")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        userDetails = CustomUserDetails.from(user);

        createToken();
    }

    private void createToken() {
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        oAuthTokenDto = OAuthTokenDto.of(TokenDto.of(accessToken, refreshToken), true);
    }

    @Test
    @DisplayName("회원가입 테스트")
    void signUpTest() throws Exception {
        // given
        SignUpDto.OAuth signUpDto = new SignUpDto.OAuth("test");

        given(oAuthCommandService.signUp(userId, signUpDto)).willReturn(oAuthTokenDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/oauth/sign-up")
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsBytes(signUpDto))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."))
                .andExpect(jsonPath("$.data.accessToken").value(oAuthTokenDto.tokenDto().accessToken()));
    }

    @Test
    @DisplayName("회원가입 테스트 - 유효성 검사 실패")
    void signUpInvalidFailedTest() throws Exception {
        // given
        SignUpDto.OAuth signUpDto = new SignUpDto.OAuth(null);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/oauth/sign-up")
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsBytes(signUpDto))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nickname").value("닉네임은 필수 입력 값입니다."));
    }

    @Test
    @DisplayName("회원가입 테스트 - 조건 불충족")
    void signUpInsufficientTest() throws Exception {
        // given
        SignUpDto.OAuth signUpDto = new SignUpDto.OAuth("x");

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/oauth/sign-up")
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsBytes(signUpDto))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nickname").value("닉네임은 특수문자를 제외한 2~10자리여야 합니다."));
    }
}
