package kr.co.yournews.apis.post.controller;

import kr.co.yournews.apis.post.dto.PostInfoDto;
import kr.co.yournews.apis.post.service.PostCommandService;
import kr.co.yournews.apis.post.service.PostQueryService;
import kr.co.yournews.domain.post.type.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostCommandService postCommandService;

    @MockBean
    private PostQueryService postQueryService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    @DisplayName("특정 게시글 조회 메서드")
    void getPostById() throws Exception {
        // given
        Long postId = 1L;
        PostInfoDto.Details postInfoDto =
                new PostInfoDto.Details(postId, "title", "content", "nickname", LocalDateTime.now());

        given(postQueryService.getPostById(postId)).willReturn(postInfoDto);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/posts/{postId}", postId)
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(postInfoDto.id()))
                .andExpect(jsonPath("$.data.title").value(postInfoDto.title()))
                .andExpect(jsonPath("$.data.content").value(postInfoDto.content()))
                .andExpect(jsonPath("$.data.nickname").value(postInfoDto.nickname()));
    }

    @Test
    @DisplayName("카테고리별 게시글 조회 메서드")
    void getPostsByCategory() throws Exception {
        // given
        Category category = Category.NEWS_REQUEST;

        Page<PostInfoDto.Summary> postInfoDtos = new PageImpl<>(List.of(
                new PostInfoDto.Summary(1L, "title1", "nickname1", LocalDateTime.now()),
                new PostInfoDto.Summary(2L, "title2", "nickname2", LocalDateTime.now()),
                new PostInfoDto.Summary(3L, "title3", "nickname3", LocalDateTime.now())
        ));

        given(postQueryService.getPostsByCategory(eq(category), any(Pageable.class))).willReturn(postInfoDtos);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/v1/posts")
                        .param("category", String.valueOf(category))
                        .param("page", "0")
                        .param("size", "10")
        );

        // then
        resultActions
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.content[0].title").value(postInfoDtos.getContent().get(0).title()))
                .andExpect(jsonPath("$.data.content[1].title").value(postInfoDtos.getContent().get(1).title()))
                .andExpect(jsonPath("$.data.content[2].title").value(postInfoDtos.getContent().get(2).title()));
    }
}
