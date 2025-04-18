package kr.co.yournews.apis.post.controller;

import jakarta.validation.Valid;
import kr.co.yournews.apis.post.dto.PostDto;
import kr.co.yournews.apis.post.service.PostCommandService;
import kr.co.yournews.apis.post.service.PostQueryService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.success.SuccessResponse;
import kr.co.yournews.domain.post.type.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    @PostMapping
    public ResponseEntity<?> createPost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestBody @Valid PostDto.Request postDto) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        postCommandService.createPost(userDetails.getUserId(), postDto)
                )
        );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @PathVariable Long postId) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        postQueryService.getPostById(postId, userDetails.getUserId())
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getPostsByCategory(@RequestParam Category category,
                                                @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        postQueryService.getPostsByCategory(category, pageable)
                )
        );
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestBody @Valid PostDto.Request postDto,
                                        @PathVariable Long postId) {
        postCommandService.updatePost(userDetails.getUserId(), postId, postDto);

        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable Long postId) {
        postCommandService.deletePost(userDetails.getUserId(), postId);

        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
