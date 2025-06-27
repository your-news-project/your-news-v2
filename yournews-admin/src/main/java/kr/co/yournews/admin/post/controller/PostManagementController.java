package kr.co.yournews.admin.post.controller;

import jakarta.validation.Valid;
import kr.co.yournews.admin.post.dto.PostDto;
import kr.co.yournews.admin.post.service.PostManagementCommandService;
import kr.co.yournews.admin.post.service.PostManagementQueryService;
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
@RequestMapping("/api/v1/admin/posts")
@RequiredArgsConstructor
public class PostManagementController {
    private final PostManagementCommandService postManagementCommandService;
    private final PostManagementQueryService postManagementQueryService;

    @PostMapping
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostDto.Request request) {

        return ResponseEntity.ok(
                SuccessResponse.from(
                        postManagementCommandService.createPost(userDetails.getUserId(), request)
                )
        );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        postManagementQueryService.getPostById(postId)
                )
        );
    }

    @GetMapping
    public ResponseEntity<?> getPostsByCategory(
            @RequestParam Category category,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        postManagementQueryService.getPostsByCategory(category, pageable)
                )
        );
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostDto.Request request,
            @PathVariable Long postId
    ) {
        postManagementCommandService.updatePost(userDetails.getUserId(), postId, request);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        postManagementCommandService.deletePost(postId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
