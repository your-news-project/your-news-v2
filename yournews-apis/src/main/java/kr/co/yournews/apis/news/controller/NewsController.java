package kr.co.yournews.apis.news.controller;

import kr.co.yournews.apis.news.service.NewsQueryService;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsQueryService newsQueryService;

    @GetMapping
    public ResponseEntity<?> getAllNews() {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        newsQueryService.getAllNews()
                )
        );
    }
}
