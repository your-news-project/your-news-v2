package kr.co.yournews.apis.post.service;

import kr.co.yournews.apis.post.dto.PostDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.post.entity.Post;
import kr.co.yournews.domain.post.exception.PostErrorType;
import kr.co.yournews.domain.post.service.PostService;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostCommandServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostCommandService postCommandService;

    private final Long userId = 1L;
    private final Long postId = 1L;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("test")
                .nickname("테스터")
                .build();
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPostTest() {
        // given
        PostDto.Request postDto = new PostDto.Request("title", "content");

        given(userService.readById(userId)).willReturn(Optional.ofNullable(user));
        given(postService.save(any(Post.class))).willReturn(1L);

        // when
        PostDto.Response result = postCommandService.createPost(userId, postDto);

        // then
        verify(userService, times(1)).readById(userId);
        assertEquals(postId, result.id());
    }

    @Test
    @DisplayName("게시글 생성 실패 - 존재하지 않는 사용자")
    void postCreateFailUserNotFoundTest() {
        // given
        PostDto.Request postDto = new PostDto.Request("title", "content");

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> postCommandService.createPost(userId, postDto));

        // then
        assertEquals(UserErrorType.NOT_FOUND, exception.getErrorType());
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePostTest() {
        // given
        PostDto.Request updatePostDto = new PostDto.Request("update title", "update content");
        Post mockPost = mock(Post.class);
        given(postService.readById(postId)).willReturn(Optional.ofNullable(mockPost));
        given(mockPost.isAuthor(userId)).willReturn(true);

        // when
        postCommandService.updatePost(userId, postId, updatePostDto);

        // then
        verify(mockPost, times(1)).updateInfo("update title", "update content");
    }

    @Test
    @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글")
    void updatePostFailPostNotFoundTest() {
        // given
        PostDto.Request updatePostDto = new PostDto.Request("title", "content");
        given(postService.readById(postId)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                postCommandService.updatePost(userId, postId, updatePostDto));

        // then
        assertEquals(PostErrorType.NOT_FOUND, exception.getErrorType());
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자가 아님")
    void updatePostFailForbiddenTest() {
        // given
        PostDto.Request updatePostDto = new PostDto.Request("title", "content");
        Post mockPost = mock(Post.class);
        given(postService.readById(postId)).willReturn(Optional.of(mockPost));
        given(mockPost.isAuthor(userId)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                postCommandService.updatePost(userId, postId, updatePostDto));

        // then
        assertEquals(PostErrorType.FORBIDDEN, exception.getErrorType());
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePostTest() {
        // given
        Post mockPost = mock(Post.class);
        given(postService.readById(postId)).willReturn(Optional.ofNullable(mockPost));
        given(mockPost.isAuthor(userId)).willReturn(true);

        // when
        postCommandService.deletePost(userId, postId);

        // then
        verify(postService, times(1)).deleteById(postId);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글")
    void deletePostFailPostNotFoundTest() {
        // given
        given(postService.readById(postId)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                postCommandService.deletePost(userId, postId));

        // then
        assertEquals(PostErrorType.NOT_FOUND, exception.getErrorType());
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 작성자가 아님")
    void deletePostFailForbiddenTest() {
        // given
        Post mockPost = mock(Post.class);
        given(postService.readById(postId)).willReturn(Optional.of(mockPost));
        given(mockPost.isAuthor(userId)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                postCommandService.deletePost(userId, postId));

        // then
        assertEquals(PostErrorType.FORBIDDEN, exception.getErrorType());
    }
}
