package kr.co.yournews.apis.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.auth.dto.SignOutDto;
import kr.co.yournews.apis.auth.service.AuthCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.type.Role;
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

import static kr.co.yournews.common.util.AuthConstants.AUTHORIZATION;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class SecuredAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthCommandService authCommandService;

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
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        userDetails = CustomUserDetails.from(user);
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void signOutTest() throws Exception {
        // given
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        SignOutDto signOutDto = new SignOutDto("device-info");

        doNothing().when(authCommandService).signOut(accessToken, refreshToken, userId, signOutDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/auth/sign-out")
                        .with(user(userDetails))
                        .header("X-Refresh-Token", refreshToken)
                        .header(AUTHORIZATION, accessToken)
                        .content(objectMapper.writeValueAsBytes(signOutDto))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
    }
}
