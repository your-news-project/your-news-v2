package kr.co.yournews.apis.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.user.dto.UserReq;
import kr.co.yournews.apis.user.service.UserCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class SecuredUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserCommandService userCommandService;

    private User user;
    private UserDetails userDetails;
    private final Long userId = 1L;

    @BeforeEach
    void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(put("/**").with(csrf()))
                .defaultRequest(delete("/**").with(csrf()))
                .build();


        user = User.builder()
                .username("test")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        userDetails = CustomUserDetails.from(user);
    }

    @Nested
    @DisplayName("비밀번호 변경")
    class UpdatePasswordTest {

        @Test
        @DisplayName("성공")
        void updatePasswordSuccess() throws Exception {
            // given
            UserReq.UpdatePassword dto =
                    new UserReq.UpdatePassword("oldPass123!", "newPass456!");

            doNothing().when(userCommandService).updatePassword(userId, dto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    patch("/api/v1/users/password")
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
        @DisplayName("실패 - 유효성 검사 실패")
        void updatePasswordInvalidFailed() throws Exception {
            // given
            UserReq.UpdatePassword dto = new UserReq.UpdatePassword(null, null);

            // when
            ResultActions resultActions = mockMvc.perform(
                    patch("/api/v1/users/password")
                            .with(user(userDetails))
                            .content(objectMapper.writeValueAsBytes(dto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.currentPassword").value("현재 비밀번호를 입력해주세요."))
                    .andExpect(jsonPath("$.errors.newPassword").value("새로운 비밀번호를 입력해주세요."));
        }

        @Test
        @DisplayName("실패 - 조건 불충족")
        void updatePasswordInsufficient() throws Exception {
            // given
            UserReq.UpdatePassword dto = new UserReq.UpdatePassword("oldPass123!", "pass");

            // when
            ResultActions resultActions = mockMvc.perform(
                    patch("/api/v1/users/password")
                            .with(user(userDetails))
                            .content(objectMapper.writeValueAsBytes(dto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.newPassword").value("비밀번호는 8~16자 영문, 숫자, 특수문자를 사용하세요."));
        }
    }

    @Test
    @DisplayName("사용자 삭제 테스트")
    void deleteUserTest() throws Exception {
        // given
        doNothing().when(userCommandService).deleteUser(userId);

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/v1/users")
                        .with(user(userDetails))
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
    }
}
