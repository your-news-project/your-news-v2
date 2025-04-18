package kr.co.yournews.apis.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.post.dto.PostDto;
import kr.co.yournews.apis.post.dto.PostInfoDto;
import kr.co.yournews.apis.post.service.PostCommandService;
import kr.co.yournews.apis.post.service.PostQueryService;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
public class SecuredPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostCommandService postCommandService;

    @MockBean
    private PostQueryService postQueryService;

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
    @DisplayName("게시글 생성")
    class CreatePostTest {

        @Test
        @DisplayName("성공")
        void createPostSuccess() throws Exception {
            // given
            PostDto.Request postDto = new PostDto.Request("게시글 제목", "게시글 내용");
            PostDto.Response response = PostDto.Response.of(1L);
            given(postCommandService.createPost(userId, postDto)).willReturn(response);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/posts")
                            .with(user(userDetails))
                            .content(objectMapper.writeValueAsBytes(postDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."))
                    .andExpect(jsonPath("$.data.id").value(1L));
        }

        @Test
        @DisplayName("실패 - 조건 불충족")
        void createPostInsufficient() throws Exception {
            // given
            PostDto.Request postDto = new PostDto.Request(null, null);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/posts")
                            .with(user(userDetails))
                            .content(objectMapper.writeValueAsBytes(postDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.title").value("제목은 필수 입력입니다."))
                    .andExpect(jsonPath("$.errors.content").value("내용은 필수 입력입니다."));
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdatePostTest {

        @Test
        @DisplayName("성공")
        void updatePostSuccess() throws Exception {
            // given
            PostDto.Request updateDto = new PostDto.Request("수정된 제목", "수정된 내용");

            // when
            ResultActions result = mockMvc.perform(
                    put("/api/v1/posts/{postId}", postId)
                            .with(user(userDetails))
                            .content(objectMapper.writeValueAsBytes(updateDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void updatePostForbidden() throws Exception {
            // given
            PostDto.Request updateDto = new PostDto.Request("수정된 제목", "수정된 내용");

            doThrow(new CustomException(PostErrorType.FORBIDDEN))
                    .when(postCommandService)
                    .updatePost(userId, postId, updateDto);

            // when
            ResultActions result = mockMvc.perform(
                    put("/api/v1/posts/{postId}", postId)
                            .with(user(userDetails))
                            .content(objectMapper.writeValueAsBytes(updateDto))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(PostErrorType.FORBIDDEN.getCode()))
                    .andExpect(jsonPath("$.message").value(PostErrorType.FORBIDDEN.getMessage()));
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeletePostTest {

        @Test
        @DisplayName("성공")
        void deletePostSuccess() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    delete("/api/v1/posts/{postId}", postId)
                            .with(user(userDetails))
            );

            // then
            result.andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void deletePostForbidden() throws Exception {
            // given
            doThrow(new CustomException(PostErrorType.FORBIDDEN))
                    .when(postCommandService)
                    .deletePost(userId, postId);

            // when
            ResultActions result = mockMvc.perform(
                    delete("/api/v1/posts/{postId}", postId)
                            .with(user(userDetails))
            );

            // then
            result.andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(PostErrorType.FORBIDDEN.getCode()))
                    .andExpect(jsonPath("$.message").value(PostErrorType.FORBIDDEN.getMessage()));
        }
    }

    @Test
    @DisplayName("특정 게시글 조회 메서드")
    void getPostById() throws Exception {
        // given
        Long postId = 1L;
        PostInfoDto.Details postInfoDto =
                new PostInfoDto.Details(postId, "title", "content", "nickname",
                        LocalDateTime.now(), userId, 10L, true);

        given(postQueryService.getPostById(postId, userId)).willReturn(postInfoDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/posts/{postId}", postId)
                        .with(user(userDetails))
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(postInfoDto.id()))
                .andExpect(jsonPath("$.data.title").value(postInfoDto.title()))
                .andExpect(jsonPath("$.data.content").value(postInfoDto.content()))
                .andExpect(jsonPath("$.data.nickname").value(postInfoDto.nickname()))
                .andExpect(jsonPath("$.data.userId").value(postInfoDto.userId()));
    }
}
