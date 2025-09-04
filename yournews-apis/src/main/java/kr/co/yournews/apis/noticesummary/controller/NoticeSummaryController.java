package kr.co.yournews.apis.noticesummary.controller;

import kr.co.yournews.apis.noticesummary.service.NoticeSummaryQueryService;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NoticeSummaryController {
    private final NoticeSummaryQueryService noticeSummaryQueryService;

    @GetMapping("/summary")
    public ResponseEntity<?> getNoticeSummaryByUrl(@RequestParam String url) {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        noticeSummaryQueryService.getNoticeSummaryByUrl(url)
                )
        );
    }
}
