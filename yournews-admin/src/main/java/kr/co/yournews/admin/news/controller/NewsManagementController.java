package kr.co.yournews.admin.news.controller;

import jakarta.validation.Valid;
import kr.co.yournews.admin.news.dto.NewsReq;
import kr.co.yournews.admin.news.service.NewsManagementCommandService;
import kr.co.yournews.admin.news.service.NewsManagementQueryService;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/news")
@RequiredArgsConstructor
public class NewsManagementController {
    private final NewsManagementCommandService newsManagementCommandService;
    private final NewsManagementQueryService newsManagementQueryService;

    @PostMapping
    public ResponseEntity<?> createNews(@RequestBody @Valid NewsReq newsReq) {
        newsManagementCommandService.createNews(newsReq);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @DeleteMapping("{newsId}")
    public ResponseEntity<?> deleteNews(@PathVariable Long newsId) {
        newsManagementCommandService.deleteNews(newsId);
        return ResponseEntity.ok(SuccessResponse.ok());
    }

    @GetMapping
    public ResponseEntity<?> getAllNews(
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        newsManagementQueryService.getAllNews(pageable)
                )
        );
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<?> getNewsInfoById(@PathVariable Long newsId) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        newsManagementQueryService.getNewsInfoById(newsId)
                )
        );
    }
}
