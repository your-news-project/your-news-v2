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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeCommandService {
    private final PostLikeService postLikeService;
    private final UserService userService;
    private final PostService postService;

    /**
     * 게시글에 좋아요를 등록하는 메서드
     * - 이미 좋아요를 눌렀다면 예외 발생
     *
     * @param userId : 사용자 ID
     * @param postId : 게시글 ID
     * @throws CustomException ALREADY_LIKED: 이미 좋아요를 눌렀을 경우
     *                         UserErrorType.NOT_FOUND: 존재하지 않는 사용자
     *                         PostErrorType.NOT_FOUND: 존재하지 않는 게시글
     */
    @Transactional
    public void likePost(Long userId, Long postId) {
        if (postLikeService.existsByUserIdAndPostId(userId, postId)) {
            throw new CustomException(PostErrorType.ALREADY_LIKED);
        }

        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        Post post = postService.readById(postId)
                .orElseThrow(() -> new CustomException(PostErrorType.NOT_FOUND));

        PostLike postLike = PostLike.builder()
                .user(user)
                .post(post)
                .build();

        postLikeService.save(postLike);
    }

    /**
     * 게시글 좋아요를 취소하는 메서드
     *
     * @param userId : 사용자 ID
     * @param postId : 게시글 ID
     */
    @Transactional
    public void unlikePost(Long userId, Long postId) {
        postLikeService.deleteByUserIdAndPostId(userId, postId);
    }
}
