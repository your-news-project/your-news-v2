package kr.co.yournews.apis.news.service;

import kr.co.yournews.apis.news.dto.SubNewsDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.entity.Keyword;
import kr.co.yournews.domain.news.entity.News;
import kr.co.yournews.domain.news.entity.SubNews;
import kr.co.yournews.domain.news.service.KeywordService;
import kr.co.yournews.domain.news.service.NewsService;
import kr.co.yournews.domain.news.service.SubNewsService;
import kr.co.yournews.domain.news.type.KeywordType;
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
    private final KeywordService keywordService;

    /**
     * 사용자가 여러 뉴스 항목을 구독하도록 처리하는 메서드
     * - 뉴스 구독 정보를 저장
     * - '영대소식'에 해당하는 구독이 있을 경우, 키워드도 함께 저장
     *
     * @param user     : 현재 로그인한 사용자 객체
     * @param newsIds  : 구독할 뉴스 ID 리스트
     * @param keywords : '영대소식' 키워드 리스트
     */
    public void subscribeToNews(User user, List<Long> newsIds, List<String> keywords) {
        List<News> newsList = newsService.readAllByIds(newsIds);

        List<SubNews> subNewsList = createSubNewsList(user, newsList);
        subNewsService.saveAll(subNewsList);

        if (!keywords.isEmpty()) {
            saveKeywordsIfYuNewsExists(subNewsList, keywords);
        }
    }

    /**
     * 뉴스 목록을 기반으로 SubNews 리스트를 생성
     *
     * @param user     : 사용자
     * @param newsList : 뉴스 목록
     * @return : SubNews 리스트
     */
    private List<SubNews> createSubNewsList(User user, List<News> newsList) {
        return newsList.stream()
                .map(news -> SubNews.builder()
                        .user(user)
                        .news(news)
                        .newsName(news.getName())
                        .build())
                .toList();
    }

    /**
     * SubNews 목록 중 '영대소식' 구독이 있을 경우, 해당 구독에 키워드를 연결해 저장
     *
     * @param subNewsList : 구독 리스트
     * @param keywords    : 키워드 문자열 리스트
     */
    private void saveKeywordsIfYuNewsExists(List<SubNews> subNewsList, List<String> keywords) {
        SubNews yuNews = subNewsList.stream()
                .filter(sub -> sub.getNews().isYuNews())
                .findFirst()
                .get();

        List<Keyword> keywordEntities = keywords.stream()
                .map(label -> Keyword.builder()
                        .keywordType(KeywordType.fromLabel(label))
                        .subNews(yuNews)
                        .build())
                .toList();

        keywordService.saveAll(keywordEntities);
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
        keywordService.deleteAllByUserId(userId);
        subNewsService.deleteAllByUserId(userId);

        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        this.subscribeToNews(user, subNewsDto.ids(), subNewsDto.keywords());
    }
}
