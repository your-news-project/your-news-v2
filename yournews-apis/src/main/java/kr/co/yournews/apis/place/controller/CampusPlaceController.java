package kr.co.yournews.apis.place.controller;

import kr.co.yournews.apis.place.service.CampusPlaceQueryService;
import kr.co.yournews.common.response.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class CampusPlaceController {
    private final CampusPlaceQueryService campusPlaceQueryService;

    @GetMapping
    public ResponseEntity<?> getAllCampusPlace() {
        return ResponseEntity.ok(
                SuccessResponse.from(
                        campusPlaceQueryService.getAllCampusPlace()
                )
        );
    }
}
