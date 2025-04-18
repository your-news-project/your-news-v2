package kr.co.yournews.apis.news.service;

import kr.co.yournews.apis.news.dto.SubNewsDto;
import kr.co.yournews.domain.news.entity.News;
import kr.co.yournews.domain.news.entity.SubNews;
import kr.co.yournews.domain.news.service.SubNewsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class SubNewsQueryServiceTest {

    @Mock
    private SubNewsService subNewsService;

    @InjectMocks
    private SubNewsQueryService subNewsQueryService;

    @Test
    @DisplayName("구독한 소식 불러오기 테스트")
    void getAllSubNewsSuccess() {
        // given
        Long userId = 1L;
        News news1 = News.builder().name("이름1").build();
        News news2 = News.builder().name("이름2").build();
        List<SubNews> subs = List.of(
                SubNews.builder().news(news1).newsName(news1.getName()).build(),
                SubNews.builder().news(news2).newsName(news2.getName()).build()
        );

        given(subNewsService.readByUserId(userId)).willReturn(subs);

        // when
        List<SubNewsDto.Response> result = subNewsQueryService.getAllSubNews(userId);

        // then
        assertThat(result).hasSize(2);
        assertEquals(news1.getName(), result.get(0).newsName());
        assertEquals(news2.getName(), result.get(1).newsName());
    }
}
