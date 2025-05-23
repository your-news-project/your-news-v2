package kr.co.yournews.apis.post.service;

import kr.co.yournews.apis.post.dto.PostInfoDto;
import kr.co.yournews.domain.post.dto.PostQueryDto;
import kr.co.yournews.domain.post.service.PostService;
import kr.co.yournews.domain.post.type.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostQueryServiceTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostQueryService postQueryService;

    @Test
    @DisplayName("특정 게시글 조회 메서드")
    void getPostByIdTest() {
        // given
        Long postId = 1L;
        Long userId = 1L;
        PostQueryDto.Details postQueryDto =
                new PostQueryDto.Details(postId, "title", "content",
                        "nickname", LocalDateTime.now(), userId, 10L, true);
        PostInfoDto.Details postInfoDto = PostInfoDto.Details.from(postQueryDto);

        given(postService.readDetailsById(postId, userId)).willReturn(Optional.of(postQueryDto));

        // when
        PostInfoDto.Details result = postQueryService.getPostById(postId, userId);

        // then
        verify(postService, times(1)).readDetailsById(postId, userId);
        assertEquals(postInfoDto.id(), result.id());
        assertEquals(postInfoDto.title(), result.title());
        assertEquals(postInfoDto.content(), result.content());
        assertEquals(postInfoDto.nickname(), result.nickname());
        assertEquals(postInfoDto.createdAt(), result.createdAt());
        assertEquals(postInfoDto.userId(), result.userId());
    }

    @Test
    @DisplayName("카테고리별 게시글 조회 메서드")
    void getPostsByCategory() {
        // given
        Category category = Category.NEWS_REQUEST;
        Pageable pageable = PageRequest.of(0, 10);

        Page<PostQueryDto.Summary> postQueries = new PageImpl<>(List.of(
                new PostQueryDto.Summary(1L, "title1", "nickname1", LocalDateTime.now(), 10L),
                new PostQueryDto.Summary(2L, "title2", "nickname2", LocalDateTime.now(), 15L),
                new PostQueryDto.Summary(3L, "title3", "nickname3", LocalDateTime.now(), 20L)
        ));
        Page<PostInfoDto.Summary> postDtos = postQueries.map(PostInfoDto.Summary::from);

        given(postService.readByCategory(category, pageable)).willReturn(postQueries);

        // when
        Page<PostInfoDto.Summary> result = postQueryService.getPostsByCategory(category, pageable);

        // then
        verify(postService, times(1)).readByCategory(category, pageable);
        assertEquals(postDtos.getSize(), result.getSize());
        assertEquals(postDtos.getTotalElements(), result.getTotalElements());
    }
}
