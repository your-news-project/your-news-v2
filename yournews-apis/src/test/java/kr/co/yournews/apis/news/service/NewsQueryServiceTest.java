package kr.co.yournews.apis.news.service;

import kr.co.yournews.apis.news.dto.NewsInfoDto;
import kr.co.yournews.domain.news.entity.News;
import kr.co.yournews.domain.news.service.NewsService;
import kr.co.yournews.domain.news.type.College;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class NewsQueryServiceTest {

    @Mock
    private NewsService newsService;

    @InjectMocks
    private NewsQueryService newsQueryService;

    @Test
    @DisplayName("전체 뉴스 목록 조회 - 성공")
    void getAllNewsSuccess() {
        // given
        List<News> newsList = List.of(
                News.builder().name("이름1").url("https://test1.com").college(College.ENGINEERING).build(),
                News.builder().name("이름2").url("https://test2.com").college(College.ARTS).build()
        );

        given(newsService.readAll()).willReturn(newsList);

        // when
        List<NewsInfoDto> result = newsQueryService.getAllNews();

        // then
        assertEquals(2, result.size());
        assertEquals("이름1", result.get(0).name());
        assertEquals("이름2", result.get(1).name());
    }
}
