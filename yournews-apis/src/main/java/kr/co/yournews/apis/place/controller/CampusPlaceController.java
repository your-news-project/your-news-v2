package kr.co.yournews.apis.place.controller;

import kr.co.yournews.apis.place.service.CampusPlaceQueryService;
import kr.co.yournews.common.response.success.SuccessResponse;
import kr.co.yournews.domain.place.type.PlaceType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class CampusPlaceController {
    private final CampusPlaceQueryService campusPlaceQueryService;

    @GetMapping
    public ResponseEntity<?> getCampusPlaceByPlaceType(
            @RequestParam(required = false) PlaceType type
    ) {
        // 앱 업데이트 전 방어적 처리
        if (type == null) {
            return ResponseEntity.ok(
                    SuccessResponse.from(
                            campusPlaceQueryService.getCampusPlaceByPlaceType(PlaceType.BUILDING)
                    )
            );
        }

        return ResponseEntity.ok(
                SuccessResponse.from(
                        campusPlaceQueryService.getCampusPlaceByPlaceType(type)
                )
        );
    }
}
