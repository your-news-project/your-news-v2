package kr.co.yournews.apis.menu.controller;

import kr.co.yournews.apis.menu.service.MenuQueryService;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuQueryService menuQueryService;

    @GetMapping
    public ResponseEntity<?> getThisWeekMenus() {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        menuQueryService.getThisWeekMenus()
                )
        );
    }
}
