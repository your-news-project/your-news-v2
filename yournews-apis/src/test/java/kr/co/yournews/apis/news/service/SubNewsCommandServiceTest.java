package kr.co.yournews.apis.news.service;

import kr.co.yournews.apis.news.dto.SubNewsDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.entity.News;
import kr.co.yournews.domain.news.service.KeywordService;
import kr.co.yournews.domain.news.service.NewsService;
import kr.co.yournews.domain.news.service.SubNewsService;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SubNewsCommandServiceTest {

    @Mock
    private SubNewsService subNewsService;

    @Mock
    private UserService userService;

    @Mock
    private NewsService newsService;

    @Mock
    private KeywordService keywordService;

    @InjectMocks
    private SubNewsCommandService subNewsCommandService;

    private User user;
    private List<Long> newsIds;
    private List<News> newsList;
    private List<String> keywords;
    private News yuNews;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("user1")
                .build();

        newsIds = List.of(1L, 2L, 3L);
        newsList = newsIds.stream()
                .map(id -> News.builder().name("이름" + id).build())
                .toList();
        keywords = List.of("취업", "학생복지");

        yuNews = News.builder()
                .name("영대소식")
                .build();
        ReflectionTestUtils.setField(yuNews, "id", 1L);
    }

    @Test
    @DisplayName("뉴스 구독 성공")
    void subscribeToNewsSuccess() {
        // given
        SubNewsDto.Request dto = new SubNewsDto.Request(newsIds, keywords);

        given(newsService.readAllByIds(newsIds)).willReturn(newsList);

        List<News> newsList = List.of(yuNews);
        given(newsService.readAllByIds(newsIds)).willReturn(newsList);

        // when
        subNewsCommandService.subscribeToNews(user, dto.ids(), dto.keywords());

        // then
        verify(newsService, times(1)).readAllByIds(newsIds);
        verify(subNewsService, times(1)).saveAll(anyList());
        verify(keywordService, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("뉴스 구독 업데이트 성공")
    void updateSubscribeNewsSuccess() {
        // given
        Long userId = 1L;
        SubNewsDto.Request dto = new SubNewsDto.Request(newsIds, keywords);

        given(userService.readById(userId)).willReturn(Optional.of(user));
        given(newsService.readAllByIds(newsIds)).willReturn(newsList);

        List<News> newsList = List.of(yuNews);
        given(newsService.readAllByIds(newsIds)).willReturn(newsList);

        // when
        subNewsCommandService.updateSubscribeNews(userId, dto);

        // then
        verify(keywordService, times(1)).deleteAllByUserId(userId);
        verify(subNewsService, times(1)).deleteAllByUserId(userId);
        verify(userService, times(1)).readById(userId);
        verify(newsService, times(1)).readAllByIds(newsIds);
        verify(subNewsService, times(1)).saveAll(anyList());
        verify(keywordService, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("뉴스 구독 업데이트 실패 - 존재하지 않는 사용자")
    void updateSubscribeNewsUserNotFound() {
        // given
        Long userId = 1L;
        SubNewsDto.Request dto = new SubNewsDto.Request(newsIds, keywords);

        given(userService.readById(userId)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
            subNewsCommandService.updateSubscribeNews(userId, dto)
        );

        // then
        assertEquals(UserErrorType.NOT_FOUND, exception.getErrorType());
        verify(subNewsService, times(1)).deleteAllByUserId(userId);
        verify(userService, times(1)).readById(userId);
        verify(newsService, never()).readAllByIds(anyList());
        verify(subNewsService, never()).saveAll(anyList());
    }
}
