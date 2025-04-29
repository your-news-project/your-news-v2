package kr.co.yournews.apis.user.service;

import kr.co.yournews.domain.news.service.KeywordService;
import kr.co.yournews.domain.news.service.SubNewsService;
import kr.co.yournews.domain.post.service.PostLikeService;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCleanupService {
    private final UserService userService;
    private final SubNewsService subNewsService;
    private final KeywordService keywordService;
    private final PostLikeService postLikeService;

    /**
     * 소프트 삭제된 유저 및 연관 데이터(Keyword, SubNews, PostLike) 삭제
     * - 14일이 지난 soft delete User 정보 삭제
     */
    @Transactional
    public void deleteSoftDeletedUsersBefore() {
        LocalDate deletedBeforeDate = LocalDate.now().minusDays(14);
        List<Long> userIds = userService.readSoftDeleteUsersBefore(deletedBeforeDate);

        if (userIds.isEmpty()) {
            return;
        }

        keywordService.deleteAllByUserIds(userIds);
        subNewsService.deleteAllByUserIds(userIds);
        postLikeService.deleteAllByUserIds(userIds);
        userService.deleteAllByIds(userIds);

        log.info("Soft deleted user cleanup completed for users deleted before {}", deletedBeforeDate);
    }
}
