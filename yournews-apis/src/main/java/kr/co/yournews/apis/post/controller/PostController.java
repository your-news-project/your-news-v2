package kr.co.yournews.apis.post.controller;

import jakarta.validation.Valid;
import kr.co.yournews.apis.post.dto.PostDto;
import kr.co.yournews.apis.post.service.PostCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostCommandService postCommandService;

    @PostMapping
    public ResponseEntity<?> createPost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestBody @Valid PostDto.Request postDto) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        postCommandService.createPost(userDetails.getUserId(), postDto)
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
