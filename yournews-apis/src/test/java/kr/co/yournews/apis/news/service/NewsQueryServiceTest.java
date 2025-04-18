package kr.co.yournews.apis.news.service;

import kr.co.yournews.apis.news.dto.NewsInfoDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.entity.News;
import kr.co.yournews.domain.news.exception.NewsErrorType;
import kr.co.yournews.domain.news.service.NewsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class NewsQueryServiceTest {

    @Mock
    private NewsService newsService;

    @InjectMocks
    private NewsQueryService newsQueryService;

    private final Long newsId = 1L;

    @Nested
    @DisplayName("특정 뉴스 조회")
    class NewsInfoTest {

        @Test
        @DisplayName("성공")
        void getNewsInfoSuccess() {
            // given
            News news = News.builder()
                    .name("이름")
                    .url("https://test.com")
                    .build();

            given(newsService.readById(newsId)).willReturn(Optional.of(news));

            // when
            NewsInfoDto.Summary result = newsQueryService.getNewsInfo(newsId);

            // then
            assertEquals(news.getName(), result.name());
            assertEquals(news.getUrl(), result.url());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 뉴스")
        void getNewsInfoFail() {
            // given
            given(newsService.readById(newsId)).willReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> newsQueryService.getNewsInfo(newsId)
            );

            // then
            assertEquals(NewsErrorType.NOT_FOUND, exception.getErrorType());
        }
    }

    @Test
    @DisplayName("전체 뉴스 목록 조회 - 성공")
    void getAllNewsSuccess() {
        // given
        List<News> newsList = List.of(
                News.builder().name("이름1").url("https://test1.com").build(),
                News.builder().name("이름2").url("https://test2.com").build()
        );

        given(newsService.readAll()).willReturn(newsList);

        // when
        List<NewsInfoDto.Details> result = newsQueryService.getAllNews();

        // then
        assertEquals(2, result.size());
        assertEquals("이름1", result.get(0).name());
        assertEquals("이름2", result.get(1).name());
    }
}
