package kr.co.yournews.apis.news.service;

import kr.co.yournews.apis.news.dto.SubNewsDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.entity.News;
import kr.co.yournews.domain.news.entity.SubNews;
import kr.co.yournews.domain.news.service.NewsService;
import kr.co.yournews.domain.news.service.SubNewsService;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubNewsCommandService {
    private final SubNewsService subNewsService;
    private final UserService userService;
    private final NewsService newsService;

    /**
     * 사용자가 여러 뉴스 항목을 구독하도록 처리하는 메서드
     *
     * @param user    : 현재 로그인한 사용자 객체
     * @param newsIds : 구독할 뉴스 ID 리스트
     */
    public void subscribeToNews(User user, List<Long> newsIds) {
        List<News> newsList = newsService.readAllByIds(newsIds);

        List<SubNews> subNewsList = newsList.stream()
                .map(news -> SubNews.builder()
                        .user(user)
                        .news(news)
                        .newsName(news.getName())
                        .build())
                .toList();

        subNewsService.saveAll(subNewsList);
    }

    /**
     * 사용자의 전체 뉴스 구독 정보를 업데이트
     * - 기존 구독 정보 삭제 후, 새로 전달된 뉴스 리스트로 구독 갱신
     *
     * @param userId     : 사용자 pk
     * @param subNewsDto : 새로 구독할 뉴스 ID 리스트
     */
    @Transactional
    public void updateSubscribeNews(Long userId, SubNewsDto.Request subNewsDto) {
        subNewsService.deleteAllByUserId(userId);

        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        this.subscribeToNews(user, subNewsDto.ids());
    }
}
