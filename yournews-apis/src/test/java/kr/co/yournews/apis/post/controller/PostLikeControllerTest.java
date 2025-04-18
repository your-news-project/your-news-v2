package kr.co.yournews.apis.post.controller;

import kr.co.yournews.apis.post.service.PostLikeCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.post.exception.PostErrorType;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostLikeController.class)
public class PostLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostLikeCommandService postLikeCommandService;

    private User user;
    private UserDetails userDetails;
    private final Long userId = 1L;
    private final Long postId = 1L;

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

    @Nested
    @DisplayName("게시글 좋아요")
    class LikePost {

        @Test
        @DisplayName("성공")
        void likePostSuccess() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/posts/{postId}/like", postId)
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
        }

        @Test
        @DisplayName("실패 - 이미 좋아요 누름")
        void likePostAlreadyLiked() throws Exception {
            // given
            doThrow(new CustomException(PostErrorType.ALREADY_LIKED))
                    .when(postLikeCommandService).likePost(userId, postId);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/posts/{postId}/like", postId)
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(PostErrorType.ALREADY_LIKED.getMessage()))
                    .andExpect(jsonPath("$.code").value(PostErrorType.ALREADY_LIKED.getCode()));
        }

        @Test
        @DisplayName("실패 - 게시글 없음")
        void likePostPostNotFound() throws Exception {
            // given
            doThrow(new CustomException(PostErrorType.NOT_FOUND))
                    .when(postLikeCommandService).likePost(userId, postId);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/posts/{postId}/like", postId)
                            .with(user(userDetails))
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(PostErrorType.NOT_FOUND.getMessage()))
                    .andExpect(jsonPath("$.code").value(PostErrorType.NOT_FOUND.getCode()));
        }
    }

    @Test
    @DisplayName("성공")
    void unlikePostSuccess() throws Exception {
        // given

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/v1/posts/{postId}/like", postId)
                        .with(user(userDetails))
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
    }
}
