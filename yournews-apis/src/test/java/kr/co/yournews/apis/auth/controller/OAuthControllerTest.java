package kr.co.yournews.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.auth.dto.OAuthCode;
import kr.co.yournews.apis.auth.dto.OAuthTokenDto;
import kr.co.yournews.apis.auth.service.OAuthCommandService;
import kr.co.yournews.auth.dto.TokenDto;
import kr.co.yournews.domain.user.type.OAuthPlatform;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OAuthController.class)
public class OAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OAuthCommandService oAuthCommandService;

    private OAuthTokenDto oAuthTokenDto;

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
        oAuthTokenDto = OAuthTokenDto.of(TokenDto.of(accessToken, refreshToken), false);
    }

    @Test
    @DisplayName("로그인 테스트")
    void signInTest() throws Exception {
        // given
        OAuthPlatform platform = OAuthPlatform.NAVER;
        OAuthCode oAuthCode = new OAuthCode("oauth-code");

        given(oAuthCommandService.signIn(platform, oAuthCode)).willReturn(oAuthTokenDto);

        // then
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/oauth/sign-in/{platform}", "NAVER")
                        .content(objectMapper.writeValueAsBytes(oAuthCode))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."))
                .andExpect(jsonPath("$.data.accessToken").value(oAuthTokenDto.tokenDto().accessToken()))
                .andExpect(jsonPath("$.data.isSignUp").value(oAuthTokenDto.isSignUp()));
    }
}
