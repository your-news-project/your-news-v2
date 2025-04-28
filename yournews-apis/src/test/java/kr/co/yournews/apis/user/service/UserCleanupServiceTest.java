package kr.co.yournews.apis.user.service;

import kr.co.yournews.domain.news.service.KeywordService;
import kr.co.yournews.domain.news.service.SubNewsService;
import kr.co.yournews.domain.post.service.PostLikeService;
import kr.co.yournews.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserCleanupServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private SubNewsService subNewsService;

    @Mock
    private KeywordService keywordService;

    @Mock
    private PostLikeService postLikeService;

    @InjectMocks
    private UserCleanupService userCleanupService;

    @Test
    @DisplayName("Soft Delete된 유저가 있을 때, 관련 데이터 삭제가 정상 동작")
    void deleteSoftDeletedUsersBeforeWhenUsersExist() {
        // given
        LocalDate deletedBeforeDate = LocalDate.now().minusDays(14);
        List<Long> userIds = List.of(1L, 2L, 3L);

        given(userService.readSoftDeleteUsersBefore(deletedBeforeDate)).willReturn(userIds);

        // when
        userCleanupService.deleteSoftDeletedUsersBefore();

        // then
        verify(userService, times(1)).readSoftDeleteUsersBefore(any(LocalDate.class));
        verify(keywordService, times(1)).deleteAllByUserIds(userIds);
        verify(subNewsService, times(1)).deleteAllByUserIds(userIds);
        verify(postLikeService, times(1)).deleteAllByUserIds(userIds);
        verify(userService, times(1)).deleteAllByIds(userIds);
    }

    @Test
    @DisplayName("Soft Delete된 유저가 없을 때, 삭제 로직이 동작 x")
    void deleteSoftDeletedUsersBeforeWhenNoUsers() {
        // given
        LocalDate deletedBeforeDate = LocalDate.now().minusDays(14);
        given(userService.readSoftDeleteUsersBefore(deletedBeforeDate)).willReturn(List.of());

        // when
        userCleanupService.deleteSoftDeletedUsersBefore();

        // then
        verify(userService, times(1)).readSoftDeleteUsersBefore(any(LocalDate.class));
        verify(keywordService, never()).deleteAllByUserIds(anyList());
        verify(subNewsService, never()).deleteAllByUserIds(anyList());
        verify(postLikeService, never()).deleteAllByUserIds(anyList());
        verify(userService, never()).deleteAllByIds(anyList());
    }
}
