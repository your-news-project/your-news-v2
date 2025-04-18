package kr.co.yournews.apis.post.service;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.post.entity.Post;
import kr.co.yournews.domain.post.entity.PostLike;
import kr.co.yournews.domain.post.exception.PostErrorType;
import kr.co.yournews.domain.post.service.PostLikeService;
import kr.co.yournews.domain.post.service.PostService;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostLikeCommandServiceTest {

    @Mock
    private PostLikeService postLikeService;

    @Mock
    private UserService userService;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostLikeCommandService postLikeCommandService;

    private final Long userId = 1L;
    private final Long postId = 10L;
    private User mockUser;
    private Post mockPost;

    @BeforeEach
    void setup() {
        mockUser = mock(User.class);
        mockPost = mock(Post.class);
    }

    @Nested
    @DisplayName("게시글 좋아요")
    class LikePost {

        @Test
        @DisplayName("성공")
        void likePostSuccess() {
            // given
            given(postLikeService.existsByUserIdAndPostId(userId, postId)).willReturn(false);
            given(userService.readById(userId)).willReturn(Optional.of(mockUser));
            given(postService.readById(postId)).willReturn(Optional.of(mockPost));

            // when
            postLikeCommandService.likePost(userId, postId);

            // then
            verify(postLikeService, times(1)).save(any(PostLike.class));
        }

        @Test
        @DisplayName("실패 - 이미 좋아요 누름")
        void alreadyLiked() {
            // given
            given(postLikeService.existsByUserIdAndPostId(userId, postId)).willReturn(true);

            // when
            CustomException exception = assertThrows(CustomException.class, () -> postLikeCommandService.likePost(userId, postId));

            // then
            assertEquals(PostErrorType.ALREADY_LIKED, exception.getErrorType());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void userNotFound() {
            // given
            given(postLikeService.existsByUserIdAndPostId(userId, postId)).willReturn(false);
            given(userService.readById(userId)).willReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> postLikeCommandService.likePost(userId, postId));

            // then
            assertEquals(UserErrorType.NOT_FOUND, exception.getErrorType());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 게시글")
        void postNotFound() {
            // given
            given(postLikeService.existsByUserIdAndPostId(userId, postId)).willReturn(false);
            given(userService.readById(userId)).willReturn(Optional.of(mockUser));
            given(postService.readById(postId)).willReturn(Optional.empty());

            // when
            CustomException exception = assertThrows(CustomException.class,
                    () -> postLikeCommandService.likePost(userId, postId));

            // then
            assertEquals(PostErrorType.NOT_FOUND, exception.getErrorType());
        }
    }

    @Test
    @DisplayName("게시글 좋아요 취소")
    void unlikePost() {
        // given

        // when
        postLikeCommandService.unlikePost(userId, postId);

        // then
        verify(postLikeService, times(1)).deleteByUserIdAndPostId(userId, postId);
    }
}
