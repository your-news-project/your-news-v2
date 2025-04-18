package kr.co.yournews.apis.news.controller;

import kr.co.yournews.apis.news.dto.NewsInfoDto;
import kr.co.yournews.apis.news.service.NewsQueryService;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.exception.NewsErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsController.class)
public class NewsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsQueryService newsQueryService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Nested
    @DisplayName("특정 뉴스 조회")
    class NewsInfoTest {

        private final Long newsId = 1L;

        @Test
        @DisplayName("성공")
        void getNewsInfoSuccess() throws Exception {
            // given
            NewsInfoDto.Summary dto = new NewsInfoDto.Summary(newsId, "이름1", "https://test.com");
            given(newsQueryService.getNewsInfo(newsId)).willReturn(dto);

            // when
            ResultActions resultActions = mockMvc.perform(
                    get("/api/v1/news/{newsId}", newsId)
            );

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."))
                    .andExpect(jsonPath("$.data.id").value(newsId))
                    .andExpect(jsonPath("$.data.name").value(dto.name()))
                    .andExpect(jsonPath("$.data.url").value(dto.url()));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 뉴스")
        void getNewsInfoFailNotFound() throws Exception {
            // given
            given(newsQueryService.getNewsInfo(newsId)).willThrow(new CustomException(NewsErrorType.NOT_FOUND));

            // when
            ResultActions result = mockMvc.perform(
                    get("/api/v1/news/{newsId}", newsId)
            );

            // then
            result.andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(NewsErrorType.NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.message").value(NewsErrorType.NOT_FOUND.getMessage()));
        }
    }

    @Test
    @DisplayName("뉴스 전체 조회 - 성공")
    void getAllNewsSuccess() throws Exception {
        // given
        List<NewsInfoDto.Details> dtoList = List.of(
                new NewsInfoDto.Details(1L, "이름1", "https://test1.com"),
                new NewsInfoDto.Details(2L, "이름2", "https://test2.com")
        );

        given(newsQueryService.getAllNews()).willReturn(dtoList);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/news"));

        // then
        result.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("요청이 성공하였습니다."))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name").value("이름1"))
                .andExpect(jsonPath("$.data[1].name").value("이름2"));
    }
}
