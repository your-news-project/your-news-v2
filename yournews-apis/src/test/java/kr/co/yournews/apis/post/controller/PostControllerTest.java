package kr.co.yournews.apis.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.post.dto.PostDto;
import kr.co.yournews.apis.post.service.PostCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.post.exception.PostErrorType;
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
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostCommandService postCommandService;

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
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        userDetails = CustomUserDetails.from(user);
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPostTest() throws Exception {
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
    @DisplayName("게시글 생성 실패 - 조건 불충족")
    void createPostInsufficientTestTest() throws Exception {
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

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePostTest() throws Exception {
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
    @DisplayName("게시글 수정 실패 - 권한 없음")
    void updatePostForbiddenTest() throws Exception {
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

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePostTest() throws Exception {
        // given

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
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void deletePostForbiddenTest() throws Exception {
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
