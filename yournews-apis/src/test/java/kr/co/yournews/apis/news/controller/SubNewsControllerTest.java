package kr.co.yournews.apis.news.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.news.dto.SubNewsDto;
import kr.co.yournews.apis.news.service.SubNewsCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
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

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubNewsController.class)
public class SubNewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubNewsCommandService subNewsCommandService;

    private User user;
    private UserDetails userDetails;
    private final Long userId = 1L;
    private SubNewsDto.Request requestDto;

    @BeforeEach
    void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .defaultRequest(put("/**").with(csrf()))
                .build();

        user = User.builder()
                .username("test")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        userDetails = CustomUserDetails.from(user);
        requestDto = new SubNewsDto.Request(List.of(1L, 2L, 3L));
    }

    @Test
    @DisplayName("뉴스 구독 수정 성공")
    void updateSubscriptionsSuccess() throws Exception {
        // given

        // when
        ResultActions resultActions = mockMvc.perform(
                put("/api/v1/news/subscription")
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
    }

    @Test
    @DisplayName("뉴스 구독 수정 실패 - 존재하지 않는 사용자")
    void updateSubscriptionsUserNotFound() throws Exception {
        // given
        doThrow(new CustomException(UserErrorType.NOT_FOUND))
                .when(subNewsCommandService).updateSubscribeNews(userId, requestDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                put("/api/v1/news/subscription")
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(UserErrorType.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(UserErrorType.NOT_FOUND.getMessage()));
    }
}
