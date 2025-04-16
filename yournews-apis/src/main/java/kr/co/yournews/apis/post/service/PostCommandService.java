package kr.co.yournews.apis.post.service;

import kr.co.yournews.apis.post.dto.PostDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.post.entity.Post;
import kr.co.yournews.domain.post.exception.PostErrorType;
import kr.co.yournews.domain.post.service.PostService;
import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.exception.UserErrorType;
import kr.co.yournews.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCommandService {
    private final UserService userService;
    private final PostService postService;

    @Transactional
    public PostDto.Response createPost(Long userId, PostDto.Request postDto) {
        User user = userService.readById(userId)
                .orElseThrow(() -> new CustomException(UserErrorType.NOT_FOUND));

        Long postId = postService.save(postDto.toEntity(user));

        return PostDto.Response.of(postId);
    }

    @Transactional
    public void updatePost(Long userId, Long postId, PostDto.Request postDto) {
        Post post = postService.readById(postId)
                .orElseThrow(() -> new CustomException(PostErrorType.NOT_FOUND));

        if (!post.isAuthor(userId)) {
            throw new CustomException(PostErrorType.FORBIDDEN);
        }

        post.updateInfo(postDto.title(), postDto.content());
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postService.readById(postId)
                .orElseThrow(() -> new CustomException(PostErrorType.NOT_FOUND));

        if (!post.isAuthor(userId)) {
            throw new CustomException(PostErrorType.FORBIDDEN);
        }

        postService.deleteById(postId);
    }
}
