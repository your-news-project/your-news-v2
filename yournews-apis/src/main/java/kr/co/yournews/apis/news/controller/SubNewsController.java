package kr.co.yournews.apis.news.controller;

import kr.co.yournews.apis.news.dto.SubNewsDto;
import kr.co.yournews.apis.news.service.SubNewsCommandService;
import kr.co.yournews.auth.authentication.CustomUserDetails;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class SubNewsController {
    private final SubNewsCommandService subNewsCommandService;

    @PutMapping("/subscription")
    public ResponseEntity<?> updateSubscriptions(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestBody SubNewsDto.Request requestDto) {

        subNewsCommandService.updateSubscribeNews(userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(SuccessResponse.ok());
    }
}
