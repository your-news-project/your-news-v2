package kr.co.yournews.apis.post.service;

import kr.co.yournews.apis.post.dto.PostInfoDto;
import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.post.dto.PostQueryDto;
import kr.co.yournews.domain.post.exception.PostErrorType;
import kr.co.yournews.domain.post.service.PostService;
import kr.co.yournews.domain.post.type.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostQueryService {
    private final PostService postService;

    /**
     * 게시글 조회 메서드
     *
     * @param postId : 조회할 게시글 pk값
     * @return : 게시글 정보
     */
    @Transactional(readOnly = true)
    public PostInfoDto.Details getPostById(Long postId) {
        PostQueryDto.Details details = postService.readDetailsById(postId)
                .orElseThrow(() -> new CustomException(PostErrorType.NOT_FOUND));

        return PostInfoDto.Details.from(details);
    }

    /**
     * 게시글 카테고리 조회 메서드
     *
     * @param category : 조회할 카테고리
     * @param pageable : 페이징
     * @return : 카테고리별 게시글 정보
     */
    @Transactional(readOnly = true)
    public Page<PostInfoDto.Summary> getPostsByCategory(Category category, Pageable pageable) {
        return postService.readByCategory(category, pageable)
                .map(PostInfoDto.Summary::from);
    }
}
