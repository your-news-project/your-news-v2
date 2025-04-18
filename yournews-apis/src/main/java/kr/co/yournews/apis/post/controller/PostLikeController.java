package kr.co.yournews.apis.post.controller;

import kr.co.yournews.apis.post.service.PostLikeCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostLikeController {
    private final PostLikeCommandService postLikeCommandService;

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable Long postId) {
        postLikeCommandService.likePost(userDetails.getUserId(), postId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<?> unlikePost(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable Long postId) {
        postLikeCommandService.unlikePost(userDetails.getUserId(), postId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
