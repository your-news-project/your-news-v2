package kr.co.yournews.admin.post.service;

import kr.co.yournews.admin.post.dto.PostDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.post.entity.Post;
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
public class PostManagementCommandService {
    private final UserService userService;
    private final PostService postService;
    private final PostLikeService postLikeService;

    /**
     * 게시글 (공지사항) 생성 메서드
     *
     * @param userId  : 게시글을 작성하는 사용자 ID (관리자)
     * @param request : 생성할 게시글의 제목 및 내용 정보를 담은 DTO
     * @return : 생성된 게시글의 ID를 담은 응답 DTO
     * @throws CustomException NOT_FOUND: 사용자가 존재하지 않을 경우
     */
    @Transactional
    public PostDto.Response createPost(Long userId, PostDto.Request request) {
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        Long postId = postService.save(request.toEntity(user));

        return PostDto.Response.of(postId);
    }

    /**
     * 게시글 (공지사항) 업데이트 메서드
     *
     * @param postId : 삭제할 게시글 ID
     * @throws CustomException NOT_FOUND: 게시글이 존재하지 않을 경우
     *                         FORBIDDEN: 작성자가 아닐 경우
     */
    @Transactional
    public void updatePost(Long userId, Long postId, PostDto.Request request) {
        Post post = postService.readById(postId)
                .orElseThrow(() -> new CustomException(PostErrorType.NOT_FOUND));

        if (!post.isAuthor(userId)) {
            throw new CustomException(PostErrorType.FORBIDDEN);
        }

        post.updateInfo(request.title(), request.content());
    }

    /**
     * 게시글 (공지사항 및 사용자 게시글) 삭제 메서드
     *
     * @param postId : 삭제할 게시글 ID
     */
    @Transactional
    public void deletePost(Long postId) {
        postLikeService.deleteAllByPostId(postId);
        postService.deleteById(postId);
    }
}
