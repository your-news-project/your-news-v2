package kr.co.yournews.apis.news.controller;

import kr.co.yournews.apis.news.dto.NewsInfoDto;
import kr.co.yournews.apis.news.service.NewsQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("뉴스 전체 조회 - 성공")
    void getAllNewsSuccess() throws Exception {
        // given
        List<NewsInfoDto> dtoList = List.of(
                new NewsInfoDto(1L, "이름1", "https://test1.com"),
                new NewsInfoDto(2L, "이름2", "https://test2.com")
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
