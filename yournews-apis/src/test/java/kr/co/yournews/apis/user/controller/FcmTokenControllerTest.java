package kr.co.yournews.apis.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.user.dto.FcmTokenReq;
import kr.co.yournews.apis.user.service.FcmTokenCommandService;
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

import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FcmTokenController.class)
public class FcmTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FcmTokenCommandService fcmTokenCommandService;

    private User user;
    private UserDetails userDetails;
    private final Long userId = 1L;

    @BeforeEach
    void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(post("/**").with(csrf()))
                .defaultRequest(delete("/**").with(csrf()))
                .build();


        user = User.builder()
                .username("test")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        userDetails = CustomUserDetails.from(user);
    }

    @Test
    @DisplayName("FCM 토큰 등록 테스트")
    void registerTokenSuccess() throws Exception {
        // given
        FcmTokenReq.Register dto = new FcmTokenReq.Register("test-token", "iPhone15");

        doNothing().when(fcmTokenCommandService).registerFcmToken(userId, dto);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/v1/fcm-token")
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
    }

    @Test
    @DisplayName("FCM 토큰 삭제 테스트")
    void deleteTokenSuccess() throws Exception {
        // given
        FcmTokenReq.Delete dto = new FcmTokenReq.Delete("iPhone15");

        doNothing().when(fcmTokenCommandService).deleteTokenByUserAndDevice(userId, dto.deviceInfo());

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/v1/fcm-token")
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
    }
}
